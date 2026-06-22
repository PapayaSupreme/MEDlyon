package structure;

public class Metro extends Transport {
    private final String lineCode;

    public Metro(String id, String lineCode) {super(id);this.lineCode = lineCode;}
    public String getLineCode() { return this.lineCode; }
    @Override
    public String getTransportType() {
        return "Metro";
    }
}