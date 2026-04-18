package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class GameBlockStateDTO {

    private Long id;
    private int value;
    private String state;
    private Long selectedByUserId;
    private String selectedUntil;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getSelectedByUserId() {
        return selectedByUserId;
    }

    public void setSelectedByUserId(Long selectedByUserId) {
        this.selectedByUserId = selectedByUserId;
    }

    public String getSelectedUntil() {
        return selectedUntil;
    }

    public void setSelectedUntil(String selectedUntil) {
        this.selectedUntil = selectedUntil;
    }
}
