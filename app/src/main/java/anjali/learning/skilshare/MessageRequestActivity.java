package anjali.learning.skilshare;

import android.os.Bundle;
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

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        setContentView(R.layout.activity_message_request);

        currentUser = getIntent().getStringExtra("currentUsername"); // pass from HomeFragment
        if(currentUser==null) { finish(); return; }

        rv = findViewById(R.id.recyclerMessages);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(this,list,currentUser);
        rv.setAdapter(adapter);

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
