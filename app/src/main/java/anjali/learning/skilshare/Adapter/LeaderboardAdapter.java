package anjali.learning.skilshare.Adapter;

import anjali.learning.skilshare.R;
import anjali.learning.skilshare.model.UserModel;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<UserModel> users;  // Use interface List here for flexibility

    public LeaderboardAdapter(List<UserModel> users) {
        this.users = new ArrayList<>(users); // copy if you want
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = users.get(position);
        holder.rankText.setText(String.valueOf(position + 1));
        holder.nameText.setText(user.getName());
        holder.xpText.setText(user.getXp() + " XP");
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateList(List<UserModel> newUsers) {
        this.users = new ArrayList<>(newUsers);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rankText, nameText, xpText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rankText = itemView.findViewById(R.id.rankText);
            nameText = itemView.findViewById(R.id.nameText);
            xpText = itemView.findViewById(R.id.xpText);
        }
    }
}
