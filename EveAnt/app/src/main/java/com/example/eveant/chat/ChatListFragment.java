package com.example.eveant.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;
import com.example.eveant.RetrofitClient;
import com.example.eveant.user.model.Profile;
import com.example.eveant.user.model.User;
import com.example.eveant.user.security.AuthManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatListFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<ChatUser> users = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        recyclerView = view.findViewById(R.id.chat_user_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(users, this::onUserClick);
        recyclerView.setAdapter(adapter);

        // Učitaj listu korisnika sa kojima je bilo dopisivanja
        loadCurrentUserAndUsers();

        return view;
    }

    /**
     * Dobavi trenutnog korisnika iz email-a → profile.username
     * Sa tim username-om pozovi /api/chat/users/{username}
     */
    private void loadCurrentUserAndUsers() {
        String email = AuthManager.getInstance(requireContext()).getEmail();
        RetrofitClient.userService.getUserByEmail(email).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getProfile() != null) {
                    String username = response.body().getProfile().getUsername();
                    Log.d("CHAT", "Current username: " + username);
                    loadUsers();
                } else {
                    Log.e("CHAT", " Nije moguće dobiti username iz profila.");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("CHAT", "Greška prilikom dobijanja korisnika", t);
            }
        });
    }

    /**
     *  Pozovi backend da dobavi sve korisnike sa kojima je bilo dopisivanja
     */
    private void loadUsers() {
        String email = AuthManager.getInstance(requireContext()).getEmail();

        RetrofitClient.chatApiService.getUsersChattedWith(email).enqueue(new Callback<List<Profile>>() {
            @Override
            public void onResponse(Call<List<Profile>> call, Response<List<Profile>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    users.clear();
                    for (Profile p : response.body()) {
                        users.add(new ChatUser(p.getUsername(), p.getEmail())); // mapiraj ako je potrebno
                    }
                    adapter.notifyDataSetChanged();
                    Log.d("CHAT", "Loaded " + users.size() + " contacts");
                } else {
                    Log.e("CHAT", "Backend vratio prazan odgovor ili grešku");
                }
            }

            @Override
            public void onFailure(Call<List<Profile>> call, Throwable t) {
                Log.e("Chat", "Failed to load users", t);
            }
        });

    }


    private void onUserClick(ChatUser user) {
        Bundle bundle = new Bundle();
        bundle.putString("username", user.getUsername());
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_chatListFragment_to_chatFragment, bundle);
    }
}
