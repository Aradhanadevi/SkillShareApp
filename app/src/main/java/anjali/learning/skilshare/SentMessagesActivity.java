package anjali.learning.skilshare;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import anjali.learning.skilshare.Adapter.SentMessageAdapter;
import anjali.learning.skilshare.model.MessageModel;

public class SentMessagesActivity extends AppCompatActivity {
    RecyclerView recyclerSent;
    SentMessageAdapter adapter;
    List<MessageModel> messages = new ArrayList<>();
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent_messages);

        // Setup RecyclerView
        recyclerSent = findViewById(R.id.recyclerSentMessages);
        recyclerSent.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SentMessageAdapter(messages);
        recyclerSent.setAdapter(adapter);

        // ✅ Get username from Intent
        String currentUser = getIntent().getStringExtra("currentUsername");
        if (currentUser == null || currentUser.isEmpty()) {
            Toast.makeText(this, "No username passed!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ✅ Point to correct branch: users/<username>/send
        dbRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser)
                .child("messages")
                .child("send");


        // ✅ Listen for messages
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MessageModel msg = ds.getValue(MessageModel.class);
                    if (msg != null) {
                        msg.id = ds.getKey(); // keep Firebase key
                        messages.add(msg);
                    }
                }
                adapter.notifyDataSetChanged();

                if (messages.isEmpty()) {
                    Toast.makeText(SentMessagesActivity.this, "No sent messages"+currentUser, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SentMessagesActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
