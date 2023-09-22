package red.jackf.whereisit.client.api;

/**
 * <p>Represents a client plugin for Where Is It.</p>
 *
 * <p>Currently matches a Runnable, however may include additional features in the future.</p>
 */
public interface WhereIsItClientPlugin {
    /**
     * Run this plugin. Use this method to hook into events under the <code>events</code> package.
     */
    void load();
}
