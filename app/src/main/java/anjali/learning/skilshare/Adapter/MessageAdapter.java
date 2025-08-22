package anjali.learning.skilshare.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.List;

import anjali.learning.skilshare.R;
import anjali.learning.skilshare.model.MessageModel;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MsgVH> {

    private final Context ctx;
    private final List<MessageModel> list;
    private final String currentUser;

    public MessageAdapter(Context c, List<MessageModel> l, String me) {
        ctx = c; list = l; currentUser = me;
    }

    @NonNull @Override public MsgVH onCreateViewHolder(@NonNull ViewGroup p,int v){
        View v1 = LayoutInflater.from(ctx).inflate(R.layout.message_card,p,false);
        return new MsgVH(v1);
    }

    @Override public void onBindViewHolder(@NonNull MsgVH h,int pos){
        MessageModel m = list.get(pos);

        h.tvFrom.setText("From: " + m.from + "\nEmail: " + (m.fromEmail != null ? m.fromEmail : "N/A"));
        h.tvMessage.setText(m.message);
        h.tvSkills.setText("You offer: "+m.myOfferedSkill+" | They offer: "+m.tutorOfferedSkill);

        // ─── Show Approved Status ───────────────────────────
        if (m.approved) {
            h.tvApproved.setVisibility(View.VISIBLE);   // ✅ show green Approved text
            h.btnAccept.setEnabled(false);             // ✅ disable button
        } else {
            h.tvApproved.setVisibility(View.GONE);     // ✅ hide text if not approved
            h.btnAccept.setEnabled(true);
        }

        // ─── Approve ───────────────────────────
        h.btnAccept.setOnClickListener(v -> {
            approveMessage(m);
            h.btnAccept.setEnabled(false);
            h.tvApproved.setVisibility(View.VISIBLE); // ✅ update UI immediately
        });

        // ─── Decline deletes the node ───────────────────────────
        h.btnDecline.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser)
                    .child("messages")
                    .child(m.id)
                    .removeValue();
        });

        // Optional: if you still want to reply separately
        h.itemView.setOnClickListener(v -> openReplyDialog(m));
    }

    @Override public int getItemCount(){ return list.size(); }

    // ───────────────────────────────────────────────
    private void approveMessage(MessageModel msg) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("users");

        // ✅ update in receiver’s messages (this is easy)
        rootRef.child(currentUser)
                .child("messages")
                .child(msg.id)
                .child("approved")
                .setValue(true);

        // ✅ find sender’s copy inside "messages/send"
        DatabaseReference senderMessages = rootRef.child(msg.from).child("messages").child("send");

        senderMessages.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    MessageModel senderMsg = child.getValue(MessageModel.class);

                    if (senderMsg == null) continue;

                    // Match by message text + timestamp (safer than only message)
                    if (senderMsg.message.equals(msg.message)
                            && senderMsg.timestamp == msg.timestamp
                            && senderMsg.to.equals(currentUser)) {

                        // ✅ update approved flag
                        child.getRef().child("approved").setValue(true);
                        break; // stop after first match
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }



    // ───────────────────────────────────────────────
    private void openReplyDialog(MessageModel msg){
        EditText et = new EditText(ctx);
        et.setText(msg.message);
        et.setSelection(et.getText().length());

        new AlertDialog.Builder(ctx)
                .setTitle("Reply to " + msg.from)
                .setView(et)
                .setPositiveButton("Send", (d,w) -> {
                    String newMsg = et.getText().toString().trim();
                    if(newMsg.isEmpty()) return;

                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference("users");

                    // Generate one common ID
                    String msgId = rootRef.child(currentUser)
                            .child("messages")
                            .child("send")
                            .push()
                            .getKey();

                    // Build object
                    MessageModel newMessage = new MessageModel(
                            msgId,
                            currentUser,
                            FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                            msg.from,
                            msg.fromEmail,
                            newMsg,
                            msg.tutorOfferedSkill,
                            msg.tutorWantedSkill,
                            msg.myOfferedSkill,
                            msg.myRequestedSkill,
                            System.currentTimeMillis(),
                            false
                    );

                    // ✅ Save to sender (messages/send)
                    rootRef.child(currentUser)
                            .child("messages")
                            .child("send")
                            .child(msgId)
                            .setValue(newMessage);

                    // ✅ Save to receiver (messages)
                    rootRef.child(msg.from)
                            .child("messages")
                            .child(msgId)
                            .setValue(newMessage);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    // ───────── ViewHolder ───────────────────────────────────────
    static class MsgVH extends RecyclerView.ViewHolder{
        TextView tvFrom,tvMessage,tvSkills,tvApproved;
        Button btnAccept, btnDecline;
        MsgVH(@NonNull View v){
            super(v);
            tvFrom    = v.findViewById(R.id.tvFrom);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvSkills  = v.findViewById(R.id.tvSkills);
            btnAccept = v.findViewById(R.id.btnAccept);
            btnDecline= v.findViewById(R.id.btnDecline);
            tvApproved= v.findViewById(R.id.tvApproved); // ✅ Added
        }
    }
}
