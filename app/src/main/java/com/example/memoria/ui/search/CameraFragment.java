package com.example.memoria.ui.search;

import static com.example.memoria.utils.BitmapUtils.decodeAndFixRotation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.memoria.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CameraFragment extends Fragment {

    private static final String TAG = "CameraFragment";
    private static final int REQ_CAMERA = 1001;

    public static final String OCR_REQUEST_KEY = "ocr_request";
    public static final String OCR_RESULT_TEXT = "ocr_text";

    private PreviewView previewView;
    private ImageView imgFreeze;
    private OcrSelectOverlayView ocrOverlay;
    private LinearLayout ocrActions;
    private TextView tvSelected;
    private Button btnSearch;

    private FloatingActionButton btnCapture;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;

    private ImageCapture imageCapture;

    private TextRecognizer recognizer;

    private volatile String selectedText = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        previewView = view.findViewById(R.id.previewView);
        imgFreeze = view.findViewById(R.id.imgFreeze);
        ocrOverlay = view.findViewById(R.id.ocrOverlay);
        ocrActions = view.findViewById(R.id.ocrActions);
        tvSelected = view.findViewById(R.id.tvSelected);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnCapture = view.findViewById(R.id.btnCapture);

        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // Nút Back
        view.findViewById(R.id.btn_camera_back).setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });


        ocrOverlay.setCallback(joinedText -> {
            selectedText = joinedText;
            tvSelected.setText(joinedText);

            ocrActions.setVisibility(joinedText.isEmpty() ? View.GONE : View.VISIBLE);
        });

        btnSearch.setOnClickListener(v -> {
            Bundle result = new Bundle();
            result.putString(OCR_RESULT_TEXT, selectedText == null ? "" : selectedText.trim());
            getParentFragmentManager().setFragmentResult(OCR_REQUEST_KEY, result);
            requireActivity().onBackPressed();
        });

        btnCapture.setOnClickListener(v -> captureAndScan());

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA);
        }
    }

    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "startCamera error", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindUseCases() {
        if (cameraProvider == null) return;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, selector, preview, imageCapture);
    }

    private void captureAndScan() {
        if (imageCapture == null) return;

        try {
            File dir = new File(requireContext().getCacheDir(), "captured");
            if (!dir.exists()) dir.mkdirs();
            File file = File.createTempFile("camerax_", ".jpg", dir);

            ImageCapture.OutputFileOptions options =
                    new ImageCapture.OutputFileOptions.Builder(file).build();

            Executor mainExecutor = ContextCompat.getMainExecutor(requireContext());

            imageCapture.takePicture(options, mainExecutor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    Log.d(TAG, "Saved: " + file.getAbsolutePath());

                    // Freeze: tắt camera để tránh nóng + giữ hình ổn định
                    if (cameraProvider != null) cameraProvider.unbindAll();

                    // Hiện ảnh đã chụp
                    Bitmap bmp = decodeAndFixRotation(file.getAbsolutePath());
                    if (bmp == null) {
                        Log.e(TAG, "decodeFile failed");
                        return;
                    }

                    // Show freeze UI
                    imgFreeze.setImageBitmap(bmp);
                    imgFreeze.setVisibility(View.VISIBLE);
                    ocrOverlay.setVisibility(View.VISIBLE);
                    btnCapture.setVisibility(View.GONE); // giống Lens: đã chụp rồi thì ẩn nút chụp

                    // OCR 1 lần
                    runOcrOnBitmap(bmp);
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Log.e(TAG, "Capture error: " + exception.getMessage(), exception);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "captureAndScan error", e);
        }
    }

    private void runOcrOnBitmap(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        recognizer.process(image)
                .addOnSuccessListener(result -> {
                    // Build words mapped to imgFreeze coordinates
                    // Lưu ý: phải đợi imgFreeze layout xong mới map chính xác.
                    imgFreeze.post(() -> {
                        List<OcrWord> words = mapWordsToImageView(result, bitmap.getWidth(), bitmap.getHeight(), imgFreeze);
                        ocrOverlay.setWords(words);
                    });
                })
                .addOnFailureListener(e -> Log.e(TAG, "OCR error: " + e.getMessage(), e));
    }

    /**
     * Map boundingBox (OCR coordinates in bitmap) -> ImageView coordinates (fitCenter).
     * imgFreeze đang scaleType=fitCenter theo layout ở trên.
     */
    private List<OcrWord> mapWordsToImageView(Text visionText, int bmpW, int bmpH, ImageView iv) {
        List<OcrWord> out = new ArrayList<>();

        int viewW = iv.getWidth();
        int viewH = iv.getHeight();
        if (viewW <= 0 || viewH <= 0) return out;

        // fitCenter: scale = min(viewW/bmpW, viewH/bmpH)
        float scale = Math.min(viewW / (float) bmpW, viewH / (float) bmpH);
        float scaledW = bmpW * scale;
        float scaledH = bmpH * scale;
        float dx = (viewW - scaledW) / 2f;
        float dy = (viewH - scaledH) / 2f;

        int lineIndex = 0;
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                for (Text.Element el : line.getElements()) {
                    Rect bb = el.getBoundingBox();
                    if (bb == null) continue;

                    String t = el.getText();
                    if (t == null) continue;
                    t = t.trim();
                    if (t.isEmpty()) continue;

                    RectF rf = new RectF(
                            bb.left * scale + dx,
                            bb.top * scale + dy,
                            bb.right * scale + dx,
                            bb.bottom * scale + dy
                    );

                    out.add(new OcrWord(t, rf, lineIndex));
                }
                lineIndex++;
            }
        }

        return out;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (cameraProvider != null) cameraProvider.unbindAll();
        if (recognizer != null) recognizer.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_CAMERA
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Log.e(TAG, "Camera permission denied");
        }
    }
}