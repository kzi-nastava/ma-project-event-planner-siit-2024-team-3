package com.example.eveant.communication;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eveant.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatFragment extends Fragment {

    private ChatViewModel viewModel;
    private MessagesAdapter adapter;
    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private ImageButton buttonSend;

    private String currentUsername= "prvider";
    private String recipientUsername = "admin";

    public ChatFragment() {
        // prazni konstruktor je OK
    }

    public static ChatFragment newInstance(String currentUser, String recipient) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("provider", currentUser);
        args.putString("admin", recipient);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentUsername = getArguments().getString("provider");
        recipientUsername = getArguments().getString("admin");


        currentUsername = "provider";
        recipientUsername = "admin";

        Log.d("ChatFragment", "currentUsername: " + currentUsername);
        Log.d("ChatFragment", "recipientUsername: " + recipientUsername);

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicijalizacija view elemenata preko prosleđenog view-a
        recyclerView = view.findViewById(R.id.recyclerViewMessages);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);

        // Inicijalizuj adapter i poveži sa RecyclerView-om
        adapter = new MessagesAdapter(new ArrayList<>(), currentUsername);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        viewModel.loadMessages(currentUsername, recipientUsername);

        viewModel.getMessages().observe(getViewLifecycleOwner(), msgs -> {
            adapter.setMessages(msgs);
            adapter.notifyDataSetChanged();
            if (adapter.getItemCount() > 0) {
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        });



        viewModel.connect(currentUsername);

        buttonSend.setOnClickListener(v -> {
            String text = editTextMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                ChatMessageDTO msg = new ChatMessageDTO();
                msg.setSenderUsername(currentUsername);
                msg.setRecipientUsername(recipientUsername);
                msg.setContent(text);
                msg.setTimestamp(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
                viewModel.sendMessage(msg);
                editTextMessage.setText("");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        /*viewModel.disconnect();*/
    }
}
