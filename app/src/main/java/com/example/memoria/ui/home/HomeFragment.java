package com.example.memoria.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memoria.R;
import com.example.memoria.data.model.dto.DatamuseWord;
import com.example.memoria.data.model.dto.DictionaryResponse;
import com.example.memoria.data.model.dto.Meaning;
import com.example.memoria.data.model.dto.Phonetic;
import com.example.memoria.data.model.dto.PublicDeck;
import com.example.memoria.service.PublicService;
import com.example.memoria.service.search.DatamuseClient;
import com.example.memoria.service.search.RetrofitClient;
import com.example.memoria.ui.profile.UserProfileViewModel;
import com.example.memoria.utils.WodCacheManager;
import dagger.hilt.android.AndroidEntryPoint;
import jakarta.inject.Inject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.memoria.ui.adapter.PublicAdapter;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import com.example.memoria.data.model.dto.WordnikWODResponse;
import com.example.memoria.service.search.WordnikService;
import com.example.memoria.utils.PronunciationManager;
import com.bumptech.glide.Glide;
@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private UserProfileViewModel userViewModel;
    @Inject
    PublicService publicService;
    private RecyclerView rvDiscover;
    private PublicAdapter adapter;
    private WodCacheManager cacheManager;
    private DictionaryResponse currentWodData;
    private ShapeableImageView ivAvatar;

    public HomeFragment() {
        // Để trống
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView tvHello = view.findViewById(R.id.tv_hello);
        ivAvatar = view.findViewById(R.id.iv_avatar);
        // 1. Ánh xạ các View trước
        TextView tvWodTitle = view.findViewById(R.id.tv_word_title);
        TextView tvWodPhonetic = view.findViewById(R.id.tv_phonetic);
        TextView tvWodMeaning = view.findViewById(R.id.tv_word_meaning);
        TextView tvWodSynonyms = view.findViewById(R.id.tv_word_synonyms);

        // 2. KHỞI TẠO cacheManager NGAY TẠI ĐÂY (Quan trọng nhất)
        cacheManager = new WodCacheManager(requireContext());

        userViewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);

        // 3. Quan sát dữ liệu người dùng để hiển thị Header
        setupUserHeaderObservers(tvHello);
        // 3. SAU ĐÓ mới được gọi hàm từ cacheManager
        DictionaryResponse cachedData = cacheManager.getCachedWod();

        if (cachedData != null) {
            currentWodData = cachedData;
            updateWodUI(currentWodData, tvWodTitle, tvWodPhonetic, tvWodMeaning, tvWodSynonyms);
        } else {
            fetchWordOfTheDay(tvWodTitle, tvWodPhonetic, tvWodMeaning, tvWodSynonyms);
        }
        // 1. Ánh xạ View
        TextView tvWords = view.findViewById(R.id.tv_words_count);
        TextView tvStreak = view.findViewById(R.id.tv_streak_count);
        rvDiscover = view.findViewById(R.id.rv_discover_decks);

        // LayoutManager dạng cuộn ngang đã được set sẵn trong XML (app:layoutManager),
        // nhưng set lại ở code cho chắc chắn và dễ kiểm soát.
        rvDiscover.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // 2. ViewModel lấy Progress (giống code ProgressFragment của bạn)
        userViewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);
        userViewModel.getLearnedToday().observe(getViewLifecycleOwner(), count -> tvWords.setText(String.valueOf(count)));
        userViewModel.getStreakLiveData().observe(getViewLifecycleOwner(), streak -> tvStreak.setText(String.valueOf(streak)));

        // 3. Navigation
        view.findViewById(R.id.btn_go_to_discover).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_deckPublicFragment);
        });

        /*view.findViewById(R.id.btn_create_deck).setOnClickListener(v -> {
            // Logic mở màn hình tạo deck mới
        });*/

        // 4. Load dữ liệu Discover từ PublicService
        setupDiscoverList();

        return view;
    }

    private void setupDiscoverList() {
        // Lấy 5 decks hiển thị ở trang chủ, truyền null cho neo phân trang và từ khóa
        int limit = 5;
        publicService.getPublicDecks(limit, null, null, (success, data, message) -> {
            // Đảm bảo chạy cập nhật UI trên Main Thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (success && data != null) {
                        List<PublicDeck> parsedDecks = new ArrayList<>();
                        for (DocumentSnapshot doc : data) {
                            PublicDeck deck = doc.toObject(PublicDeck.class);
                            if (deck != null) {
                                // Đề phòng trường hợp id trên object bị null, map luôn id của document
                                deck.setPublicDocId(doc.getId());
                                parsedDecks.add(deck);
                            }
                        }

                        // Khởi tạo Adapter với danh sách vừa parse
                        adapter = new PublicAdapter(parsedDecks, deck -> {
                            Bundle bundle = new Bundle();
                            bundle.putString("publicDocId", deck.getPublicDocId()); // Có thể không cần thiết
                            bundle.putString("searchDeckName", deck.getDeckName());
                            Navigation.findNavController(requireView()).navigate(R.id.deckPublicFragment, bundle);

                            Log.d("HomeFragment", "Clicked deck: " + deck.getDeckName());
                        });

                        rvDiscover.setAdapter(adapter);
                    } else {
                        Toast.makeText(getContext(), getString(R.string.action_fail_load_five_public_deck) + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /*private void fetchWordOfTheDay(TextView title, TextView phonetic, TextView meaning) {
        // Gợi ý: Bạn nên lưu API_KEY trong local.properties hoặc BuildConfig
        String WORDNIK_KEY = "YOUR_WORDNIK_API_KEY";

        // Khởi tạo Wordnik Retrofit (Bạn có thể tạo một Client dùng chung tương tự RetrofitClient)
        WordnikService wordnikService = new Retrofit.Builder()
                .baseUrl("https://api.wordnik.com/v4/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WordnikService.class);

        wordnikService.getWordOfTheDay(WORDNIK_KEY).enqueue(new Callback<WordnikWODResponse>() {
            @Override
            public void onResponse(Call<WordnikWODResponse> call, Response<WordnikWODResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String dailyWord = response.body().word;
                    // Sau khi có từ, dùng Dictionary API đã có để lấy chi tiết (phiên âm, audio)
                    fetchDetailedWod(dailyWord, title, phonetic, meaning);
                }
            }

            @Override
            public void onFailure(Call<WordnikWODResponse> call, Throwable t) {
                Log.e("WOD", "Fail to get word from Wordnik");
            }
        });
    }*/

    // Thêm TextView synonyms vào đây
    private void fetchWordOfTheDay(TextView title, TextView phonetic, TextView meaning, TextView synonyms) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        char startingLetter = alphabet.charAt(dayOfYear % 26);
        String pattern = startingLetter + "?????";

        DatamuseClient.getApi().getRandomWords(pattern, 50).enqueue(new Callback<List<DatamuseWord>>() {
            @Override
            public void onResponse(Call<List<DatamuseWord>> call, Response<List<DatamuseWord>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<DatamuseWord> list = response.body();
                    String selectedWord = list.get(dayOfYear % list.size()).word;

                    // Truyền thêm synonyms vào đây
                    fetchDetailedWod(selectedWord, title, phonetic, meaning, synonyms);
                }
            }
            @Override
            public void onFailure(Call<List<DatamuseWord>> call, Throwable t) {
                title.setText("Keep learning!");
            }
        });
    }
    /*private void fetchDetailedWod(String word, TextView title, TextView phonetic, TextView meaning) {
        RetrofitClient.getApi().getMeaning(word).enqueue(new Callback<List<DictionaryResponse>>() {
            @Override
            public void onResponse(Call<List<DictionaryResponse>> call, Response<List<DictionaryResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentWodData = response.body().get(0);

                    // Cập nhật UI
                    getActivity().runOnUiThread(() -> {
                        title.setText(currentWodData.word);
                        if (currentWodData.phonetics != null && !currentWodData.phonetics.isEmpty()) {
                            phonetic.setText(currentWodData.phonetics.get(0).text);
                        }
                        if (currentWodData.meanings != null && !currentWodData.meanings.isEmpty()) {
                            meaning.setText(currentWodData.meanings.get(0).definitions.get(0).definition);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<DictionaryResponse>> call, Throwable t) {
                // Fallback: Nếu Dictionary API lỗi, hiện từ từ Wordnik thôi
                title.setText(word);
            }
        });
    }*/

    // Thêm TextView synonyms vào đây
    private void fetchDetailedWod(String word, TextView title, TextView phonetic, TextView meaning, TextView synonyms) {
        RetrofitClient.getApi().getMeaning(word).enqueue(new Callback<List<DictionaryResponse>>() {
            @Override
            public void onResponse(Call<List<DictionaryResponse>> call, Response<List<DictionaryResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    currentWodData = response.body().get(0);

                    // Truyền thêm synonyms vào đây
                    updateWodUI(currentWodData, title, phonetic, meaning, synonyms);

                    cacheManager.saveWod(currentWodData);
                }
            }
            @Override
            public void onFailure(Call<List<DictionaryResponse>> call, Throwable t) {
                Log.e("WOD", "Dictionary API failed");
            }
        });
    }
    private void updateWodUI(DictionaryResponse data, TextView title, TextView phonetic, TextView meaning, TextView synonyms) {
        if (data == null || getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            title.setText(data.word);

            // 1. Gán phiên âm
            if (data.phonetics != null && !data.phonetics.isEmpty()) {
                String ipa = "";
                for (Phonetic p : data.phonetics) {
                    if (p.text != null && !p.text.isEmpty()) {
                        ipa = p.text;
                        break;
                    }
                }
                phonetic.setText(ipa);
            }

            // 2. Gán Nghĩa và Synonyms
            if (data.meanings != null && !data.meanings.isEmpty()) {
                boolean hasMeaning = false;
                for (Meaning m : data.meanings) {
                    if (m.definitions != null && !m.definitions.isEmpty()) {
                        String def = m.definitions.get(0).definition;
                        if (def != null && !def.isEmpty()) {
                            meaning.setText(def);
                            hasMeaning = true;

                            // Hiển thị Synonyms nếu có trong Meaning này
                            if (m.synonyms != null && !m.synonyms.isEmpty()) {
                                synonyms.setVisibility(View.VISIBLE);
                                // Lấy tối đa 3 từ đồng nghĩa cho đẹp giao diện
                                List<String> subList = m.synonyms.subList(0, Math.min(3, m.synonyms.size()));
                                synonyms.setText(android.text.TextUtils.join(", ", subList));
                            } else {
                                synonyms.setVisibility(View.GONE);
                            }
                            break;
                        }
                    }
                }
                if (!hasMeaning) meaning.setText("Definition not found.");
            }
        });
    }
    private void setupUserHeaderObservers(TextView tvHello) {
        // Quan sát Tên người dùng
        userViewModel.getUserName().observe(getViewLifecycleOwner(), name -> {
            if (name != null) {
                // Tách lấy tên đầu nếu muốn (VD: "Hello, Phương")
                String firstName = name.split(" ")[name.split(" ").length - 1];
                tvHello.setText("Hello, " + firstName);
            }
        });

        // Quan sát Avatar người dùng
        userViewModel.getUserAvatar().observe(getViewLifecycleOwner(), uri -> {
            if (uri != null) {
                Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_default_avatar)
                        .error(R.drawable.ic_default_avatar)
                        .circleCrop()
                        .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.ic_default_avatar);
            }
        });
    }
}