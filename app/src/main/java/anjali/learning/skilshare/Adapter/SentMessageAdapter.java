package anjali.learning.skilshare.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import anjali.learning.skilshare.R;
import anjali.learning.skilshare.model.MessageModel;

public class SentMessageAdapter extends RecyclerView.Adapter<SentMessageAdapter.ViewHolder> {

    private final List<MessageModel> messageList;

    public SentMessageAdapter(List<MessageModel> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sent_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageModel msg = messageList.get(position);

        holder.txtMessage.setText(msg.message);
        holder.txtTo.setText("To: " + (msg.toEmail != null ? msg.toEmail : msg.to));

        // ✅ Show tick if approved
        holder.imgApproved.setVisibility(msg.approved ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTo;
        ImageView imgApproved;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTo = itemView.findViewById(R.id.txtTo);
            imgApproved = itemView.findViewById(R.id.imgApproved);
        }
    }
}
