public enum Status {
    Loading("LOADING"),
    Waiting("WAITING"),
    Play("PLAY");
    
    private final String status;
    
    Status(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
