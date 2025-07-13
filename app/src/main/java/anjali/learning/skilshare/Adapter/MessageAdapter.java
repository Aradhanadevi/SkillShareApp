package anjali.learning.skilshare.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;
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

        h.tvFrom.setText("From: " + m.from);
        h.tvFrom.setText("From: " + m.from + "\nEmail: " + (m.fromEmail != null ? m.fromEmail : "N/A"));
        h.tvMessage.setText(m.message);
        h.tvSkills.setText("You offer: "+m.myOfferedSkill+" | They offer: "+m.tutorOfferedSkill);

        // ─── Decline deletes the node ───────────────────────────
        h.btnDecline.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser)
                    .child("messages")
                    .child(m.id)
                    .removeValue();
        });

        // ─── Accept / tap opens edit‑dialog ─────────────────────
        View.OnClickListener open = v -> openReplyDialog(m);
        h.btnAccept.setOnClickListener(open);
        h.itemView.setOnClickListener(open);
    }

    @Override public int getItemCount(){ return list.size(); }

    // ────────────────────────────────────────────────────────────
    private void openReplyDialog(MessageModel msg){
        EditText et = new EditText(ctx);
        et.setText(msg.message);           // pre‑fill
        et.setSelection(et.getText().length());

        new AlertDialog.Builder(ctx)
                .setTitle("Reply to "+msg.from)
                .setView(et)
                .setPositiveButton("Send", (d,w)->{
                    String newMsg = et.getText().toString().trim();
                    if(newMsg.isEmpty()) return;

                    // write reply under sender's /messages
                    DatabaseReference ref = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(msg.from)
                            .child("messages")
                            .push();

                    ref.setValue(new MessageModel(
                            ref.getKey(),
                            currentUser,
                            newMsg,
                            msg.tutorOfferedSkill,   // swap roles
                            msg.tutorWantedSkill,
                            msg.myOfferedSkill,
                            msg.myRequestedSkill,
                            System.currentTimeMillis()
                    ));
                    Toast.makeText(ctx,"Reply sent",Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel",null)
                .show();
    }

    // ───────── ViewHolder ───────────────────────────────────────
    static class MsgVH extends RecyclerView.ViewHolder{
        TextView tvFrom,tvMessage,tvSkills;
        Button btnAccept, btnDecline;
        MsgVH(@NonNull View v){
            super(v);
            tvFrom    = v.findViewById(R.id.tvFrom);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvSkills  = v.findViewById(R.id.tvSkills);
            btnAccept = v.findViewById(R.id.btnAccept);
            btnDecline= v.findViewById(R.id.btnDecline);
        }
    }
}
