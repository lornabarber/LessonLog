package ie.ucc.bis.a114355681.learnerlog.datamodel;

/**
 * Created by Lorna on 19/02/2018.
 */

public class Payment {
    private String amount;
    private String name;
    private String date;


    public Payment() {
    }

    public Payment(String amount, String name, String date) {
        this.amount = amount;
        this.name = name;
        this.date = date;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
