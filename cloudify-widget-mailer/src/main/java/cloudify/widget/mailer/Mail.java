package cloudify.widget.mailer;

import java.util.Collection;

/**
* User: eliranm
* Date: 2/12/14
* Time: 1:17 PM
*/
public class Mail {

    private final String from;
    private final Collection<String> to;
    private final String subject;
    private final String message;

    Mail(MailBuilder mailBuilder) {
        this.from = mailBuilder.from;
        this.to = mailBuilder.to;
        this.subject = mailBuilder.subject;
        this.message = mailBuilder.message;
    }

    public String from() {
        return from;
    }

    public Collection<String> to() {
        return to;
    }

    public String subject() {
        return subject;
    }

    public String message() {
        return message;
    }

    public static class MailBuilder {

        private final String from; // required
        private final Collection<String> to; // required
        private final String subject; // required
        private String message; // optional

        public MailBuilder(String from, Collection<String> to, String subject) {
            this.from = from;
            this.to = to;
            this.subject = subject;
        }

        public MailBuilder message(String message) {
            this.message = message;
            return this;
        }

        public Mail build() {
            return new Mail(this);
        }

    }

    @Override
    public String toString() {
        return "Mail{" +
                "from='" + from + '\'' +
                ", to=" + to +
                ", subject='" + subject + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
