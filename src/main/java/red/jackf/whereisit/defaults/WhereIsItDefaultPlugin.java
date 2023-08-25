package red.jackf.whereisit.defaults;

public class WhereIsItDefaultPlugin implements Runnable {
    @Override
    public void run() {
        BuiltInCriteria.setup();
        DefaultBlockSearchers.setup();
        DefaultConnectedBlocksGrabbers.setup();
        DefaultNestedItemStackSearchers.setup();
    }
}
