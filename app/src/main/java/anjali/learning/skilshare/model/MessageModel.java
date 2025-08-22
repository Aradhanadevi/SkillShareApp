package anjali.learning.skilshare.model;

// For showing messages stored in username
public class MessageModel {
    public String id;

    // sender info
    public String from;
    public String fromEmail;

    // receiver info
    public String to;
    public String toEmail;

    public String message;
    public String tutorOfferedSkill;
    public String tutorWantedSkill;
    public String myOfferedSkill;
    public String myRequestedSkill;
    public long timestamp;
    public boolean approved;   // ✅ approval flag

    // ─── Needed by Firebase ───────────────────────────────
    public MessageModel() {}

    // ─── Old constructor (backward compatibility, no email/approved) ─────────────
    public MessageModel(String id, String from, String message,
                        String tutorOfferedSkill, String tutorWantedSkill,
                        String myOfferedSkill, String myRequestedSkill,
                        long timestamp) {
        this.id = id;
        this.from = from;
        this.message = message;
        this.tutorOfferedSkill = tutorOfferedSkill;
        this.tutorWantedSkill = tutorWantedSkill;
        this.myOfferedSkill = myOfferedSkill;
        this.myRequestedSkill = myRequestedSkill;
        this.timestamp = timestamp;

        // default values for missing fields
        this.fromEmail = null;
        this.to = null;
        this.toEmail = null;
        this.approved = false;
    }

    // ─── Full constructor (with all fields) ─────────────
    public MessageModel(String id,
                        String from, String fromEmail,
                        String to, String toEmail,
                        String message,
                        String tutorOfferedSkill, String tutorWantedSkill,
                        String myOfferedSkill, String myRequestedSkill,
                        long timestamp, boolean approved) {
        this.id = id;
        this.from = from;
        this.fromEmail = fromEmail;
        this.to = to;
        this.toEmail = toEmail;
        this.message = message;
        this.tutorOfferedSkill = tutorOfferedSkill;
        this.tutorWantedSkill = tutorWantedSkill;
        this.myOfferedSkill = myOfferedSkill;
        this.myRequestedSkill = myRequestedSkill;
        this.timestamp = timestamp;
        this.approved = approved;
    }
}
