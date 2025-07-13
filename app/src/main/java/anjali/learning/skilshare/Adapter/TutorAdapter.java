package anjali.learning.skilshare.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.*;

import anjali.learning.skilshare.R;
import anjali.learning.skilshare.model.UserModel;

public class TutorAdapter extends RecyclerView.Adapter<TutorAdapter.TutorViewHolder> {

    private final Context context;
    private final ArrayList<UserModel> tutorList;
    private final String currentUsername;

    public TutorAdapter(Context ctx, ArrayList<UserModel> list, String currentUsername) {
        this.context = ctx;
        this.tutorList = list;
        this.currentUsername = currentUsername;
    }

    @NonNull
    @Override
    public TutorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.tutor_card, parent, false);
        return new TutorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TutorViewHolder holder, int pos) {
        UserModel tutor = tutorList.get(pos);
        holder.nameTV.setText(tutor.name);
        holder.emailTV.setText(tutor.email);
        holder.sendBtn.setOnClickListener(v -> openSwapDialog(tutor));
    }

    @Override
    public int getItemCount() {
        return tutorList.size();
    }

    // ──────────────────────────────────────────────
    private void openSwapDialog(UserModel tutor) {
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_swap_request, null, false);

        Spinner myOfferedSpinner = dialogView.findViewById(R.id.spinnerMyOffered);
        Spinner myRequestedSpinner = dialogView.findViewById(R.id.spinnerMyRequested);
        Spinner tutorWantedSpinner = dialogView.findViewById(R.id.spinnerTutorWanted);
        Spinner tutorOfferedSpinner = dialogView.findViewById(R.id.spinnerTutorOffered);
        EditText messageET = dialogView.findViewById(R.id.etMessage);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Load current user's skills
        usersRef.child(currentUsername)
                .addListenerForSingleValueEvent(new SimpleValue() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot meSnap) {
                        String mineOffered = meSnap.child("skilloffered").getValue(String.class);
                        String mineRequested = meSnap.child("skillrequested").getValue(String.class);

                        myOfferedSpinner.setAdapter(makeAdapter(mineOffered));
                        myRequestedSpinner.setAdapter(makeAdapter(mineRequested));
                    }
                });

        // Load tutor's skills
        usersRef.child(tutor.username)
                .addListenerForSingleValueEvent(new SimpleValue() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot tSnap) {
                        String tutorWanted = tSnap.child("skillrequested").getValue(String.class);
                        String tutorOffered = tSnap.child("skilloffered").getValue(String.class);

                        tutorWantedSpinner.setAdapter(makeAdapter(tutorWanted));
                        tutorOfferedSpinner.setAdapter(makeAdapter(tutorOffered));
                    }
                });

        // Show dialog
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Send swap request to " + tutor.name)
                .setView(dialogView)
                .setPositiveButton("Submit", (d, w) -> {
                    String offered = getSelected(myOfferedSpinner);
                    String requested = getSelected(myRequestedSpinner);
                    String tutorWants = getSelected(tutorWantedSpinner);
                    String tutorGives = getSelected(tutorOfferedSpinner);
                    String msg = messageET.getText().toString().trim();

                    pushMessageToFirebase(tutor.username, offered, requested, tutorWants, tutorGives, msg);
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void pushMessageToFirebase(String tutorUsername,
                                       String myOffered,
                                       String myRequested,
                                       String tutorWanted,
                                       String tutorOffered,
                                       String msg) {

        Map<String, Object> data = new HashMap<>();
        data.put("from", currentUsername);
        data.put("myOfferedSkill", myOffered);
        data.put("myRequestedSkill", myRequested);
        data.put("tutorWantedSkill", tutorWanted);
        data.put("tutorOfferedSkill", tutorOffered);
        data.put("message", msg);
        data.put("timestamp", ServerValue.TIMESTAMP);

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(tutorUsername)
                .child("messages")
                .push()
                .setValue(data)
                .addOnSuccessListener(v ->
                        Toast.makeText(context, "Request sent!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private ArrayAdapter<String> makeAdapter(String csv) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item,
                splitCsv(csv));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private List<String> splitCsv(String csv) {
        if (csv == null || csv.trim().isEmpty()) return Collections.singletonList("N/A");
        return Arrays.asList(csv.split("\\s*,\\s*"));
    }

    private String getSelected(Spinner sp) {
        Object o = sp.getSelectedItem();
        return o == null ? "" : o.toString();
    }

    private abstract static class SimpleValue implements ValueEventListener {
        @Override public void onCancelled(@NonNull DatabaseError error) {}
    }

    static class TutorViewHolder extends RecyclerView.ViewHolder {
        TextView nameTV, emailTV;
        Button sendBtn;

        TutorViewHolder(@NonNull View v) {
            super(v);
            nameTV = v.findViewById(R.id.tutorName);
            emailTV = v.findViewById(R.id.tutorEmail);
            sendBtn = v.findViewById(R.id.sendMessageBtn);
        }
    }
}
