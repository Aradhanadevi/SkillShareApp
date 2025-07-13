package anjali.learning.skilshare.model;
//for showing messages stored in username so th
public class MessageModel {
    public String id;
    public String from;
    public String fromEmail;
    public String message;
    public String tutorOfferedSkill;
    public String tutorWantedSkill;
    public String myOfferedSkill;
    public String myRequestedSkill;
    public long timestamp;

    public MessageModel() {}

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
    }

    // New constructor with email:
    public MessageModel(String id, String from, String fromEmail, String message,
                        String tutorOfferedSkill, String tutorWantedSkill,
                        String myOfferedSkill, String myRequestedSkill,
                        long timestamp) {
        this(id, from, message, tutorOfferedSkill, tutorWantedSkill, myOfferedSkill, myRequestedSkill, timestamp);
        this.fromEmail = fromEmail;
    }
}
