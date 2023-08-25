package red.jackf.whereisit.client.defaults;

public class WhereIsItDefaultClientPlugin implements Runnable {
    @Override
    public void run() {
        SearchRequestPopulatorDefaults.setup();
        OverlayStackBehaviorDefaults.setup();
        SearchInvokerDefaults.setup();
        ShouldIgnoreKeyDefaults.setup();
    }
}
