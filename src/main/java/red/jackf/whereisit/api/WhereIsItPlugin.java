package red.jackf.whereisit.api;

/**
 * <p>Represents a common plugin for Where Is It.</p>
 *
 * <p>Currently matches a Runnable, however may include additional features in the future.</p>
 */
public interface WhereIsItPlugin {
    /**
     * Run this plugin. Use this method to hook into events under the <code>events</code> package.
     */
    void load();
}
