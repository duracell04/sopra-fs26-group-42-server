package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class BlockSelectionMessageDTO {

    private String sessionCode;
    private Long userId;
    private Long blockId;
    private int blockValue;
    private int targetProduct;
    private Long selectionWindowMs;

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public int getBlockValue() {
        return blockValue;
    }

    public void setBlockValue(int blockValue) {
        this.blockValue = blockValue;
    }

    public int getTargetProduct() {
        return targetProduct;
    }

    public void setTargetProduct(int targetProduct) {
        this.targetProduct = targetProduct;
    }

    public Long getSelectionWindowMs() {
        return selectionWindowMs;
    }

    public void setSelectionWindowMs(Long selectionWindowMs) {
        this.selectionWindowMs = selectionWindowMs;
    }
}
