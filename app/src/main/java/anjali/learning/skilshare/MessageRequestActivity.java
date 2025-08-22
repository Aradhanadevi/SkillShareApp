package anjali.learning.skilshare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;

import anjali.learning.skilshare.Adapter.MessageAdapter;
import anjali.learning.skilshare.model.MessageModel;

public class MessageRequestActivity extends AppCompatActivity {

    private RecyclerView rv;
    private MessageAdapter adapter;
    private final ArrayList<MessageModel> list = new ArrayList<>();
    private String currentUser;
    Button sentmessage;
    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_message_request);

        currentUser = getIntent().getStringExtra("currentUsername"); // pass from HomeFragment
        if(currentUser==null) { finish(); return; }

        rv = findViewById(R.id.recyclerMessages);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(this,list,currentUser);
        rv.setAdapter(adapter);
        sentmessage=findViewById(R.id.btnSendMessage);
        sentmessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MessageRequestActivity.this, SentMessagesActivity.class);
                intent.putExtra("currentUsername", currentUser); // pass current user
                startActivity(intent);
            }
        });


        loadMessages();
    }

    private void loadMessages(){
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser)
                .child("messages");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot s : snapshot.getChildren()){
                    if (s.getKey().equals("send")) continue; // 🚫 Skip sent folder

                    MessageModel m = s.getValue(MessageModel.class);
                    if (m != null) {
                        m.id = s.getKey();
                        list.add(m);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MessageRequestActivity.this,
                        "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
