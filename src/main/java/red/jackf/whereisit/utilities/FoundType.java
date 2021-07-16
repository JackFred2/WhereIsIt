package red.jackf.whereisit.utilities;

/**
 * States how an item was found, used for different colours.
 * NOT_FOUND: The item was not found.
 * FOUND: The item was found directly.
 * FOUND_DEEP: The item was found in a nested inventory (shulker box, backpack).
 */
public enum FoundType {
    NOT_FOUND,
    FOUND,
    FOUND_DEEP
}
