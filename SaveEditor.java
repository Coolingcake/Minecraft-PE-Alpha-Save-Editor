import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class SaveEditor {
    public static int itemCount;
    public static byte[] data;
    public static int nameLength;
    public static String filePath;
    
    public static Map<String, String> blockNames = new HashMap<>();
    public static Map<String, String> itemNames = new HashMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Load CSV files
            loadBlockCSV();
            loadItemCSV();
            
            // File path input
            System.out.print("Enter file path to level.dat: ");
            filePath = scanner.nextLine();

            // Read file
            data = Files.readAllBytes(Path.of(filePath));
            System.out.println("File loaded! Size: " + data.length + " bytes.");

            // Extract metadata
            nameLength = data[59];
            itemCount = data[385 + nameLength] - 9;

            // Remove hotbar references
            removeHotbar();

            // Show and modify inventory
            displayArmor(scanner);
            if (itemCount > 0) {
                displayInventory(scanner);
            }
            if (itemCount < 36) {
				System.out.print("Do you want to add an item? (y/n): ");
				if (scanner.next().equalsIgnoreCase("y")) {
					addItem(scanner);
					while (true) {
						System.out.println("Do you want to add another item? (y/n): ");
						if (scanner.next().equalsIgnoreCase("y")) {
							addItem(scanner);
						} else {
							break;
						}
					}
				}
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

        scanner.close();
    }

    // -----------------------------
    //     INVENTORY MANAGEMENT
    // -----------------------------

    public static void displayArmor(Scanner scanner) {
        int startOffset = 109 + nameLength;
        System.out.println("\n--- ARMOR ---");

        String[] name = {
            "Helmet",
            "Chestplate", 
            "Leggings",
            "Boots"
        };

        for (int i = 0; i < 4; i++) {
            int itemOffset = startOffset + (i * 28);

            byte count = data[itemOffset + 5];
            byte dataValue = data[itemOffset + 15];
            byte id = data[itemOffset + 22];
            byte isItem = data[itemOffset + 23];

            int unsignedID = id & 0xFF;
            int unsignedData = dataValue & 0xFF;
            int unsignedCount = count & 0xFF;
            
            String itemName = getName(unsignedID, unsignedData, isItem == 1);
            
            System.out.printf("[%s] ID: %d | Count: %d | Data: %d | Type: %s | Name: %s%n",
                    name[i], unsignedID, unsignedCount, unsignedData, 
                    (isItem == 1 ? "Item" : "Block"), itemName);
        }

        System.out.println("-----------------------");

        System.out.print("Do you want to edit your armor? (y/n): ");
        if (scanner.next().equalsIgnoreCase("y")) {
            editArmor(scanner);
			while (true) {
				System.out.println("Do you want to edit another piece of armor? (y/n): ");
				if (scanner.next().equalsIgnoreCase("y")) {
					editArmor(scanner);
				} else {
					return;
				}
			}
        }
    }

    public static void displayInventory(Scanner scanner) {
        int startOffset = 716 + nameLength;
        System.out.println("\n--- INVENTORY ITEMS ---");

        for (int i = 0; i < itemCount; i++) {
            int itemOffset = startOffset + (i * 36);

            byte count = data[itemOffset + 5];
            byte dataValue = data[itemOffset + 15];
            byte id = data[itemOffset + 30];
            byte isItem = data[itemOffset + 31];

            int unsignedID = id & 0xFF;
            int unsignedData = dataValue & 0xFF;
            int unsignedCount = count & 0xFF;
            
            String itemName = getName(unsignedID, unsignedData, isItem == 1);
            
            System.out.printf("[%d] ID: %d | Count: %d | Data: %d | Type: %s | Name: %s%n",
                    i, unsignedID, unsignedCount, unsignedData,
                    (isItem == 1 ? "Item" : "Block"), itemName);
        }

        System.out.println("-----------------------");

        System.out.print("Do you want to edit an item? (y/n): ");
        if (scanner.next().equalsIgnoreCase("y")) {
            editItem(scanner);
			while (true) {
				System.out.println("Do you want to edit another item? (y/n): ");
				if (scanner.next().equalsIgnoreCase("y")) {
					editItem(scanner);
				} else {
					return;
				}
			}
        }
    }

    public static void editArmor(Scanner scanner) {
        int startOffset = 109 + nameLength;
        String[] name = {
            "Helmet",
            "Chestplate",
            "Leggings", 
            "Boots"
        };

        System.out.print("Which armor piece do you want to edit? (0=Helmet, 1=Chestplate, 2=Leggings, 3=Boots): ");
        int index = scanner.nextInt();

        if (index < 0 || index >= 4) {
            System.out.println("Invalid selection!");
            return;
        }

        int itemOffset = startOffset + (index * 28);

        byte oldCount = data[itemOffset + 5];
        byte oldData = data[itemOffset + 15];
        byte oldID = data[itemOffset + 22];
        byte oldIsItem = data[itemOffset + 23];

        int unsignedID = oldID & 0xFF;
        int unsignedData = oldData & 0xFF;
        int unsignedCount = oldCount & 0xFF;
        String oldName = getName(unsignedID, unsignedData, oldIsItem == 1);
        
        System.out.printf("Editing %s:%nCurrent -> ID: %d | Count: %d | Data: %d | Type: %s | Name: %s%n",
                name[index], unsignedID, unsignedCount, unsignedData,
                (oldIsItem == 1 ? "Item" : "Block"), oldName);

        System.out.print("New ID (or -1 to keep): ");
        int newID = scanner.nextInt();
        if (newID != -1) data[itemOffset + 22] = (byte) newID;

        System.out.print("New Count (or -1 to keep): ");
        int newCount = scanner.nextInt();
        if (newCount != -1) data[itemOffset + 5] = (byte) newCount;

        System.out.print("New Data Value (or -1 to keep): ");
        int newData = scanner.nextInt();
        if (newData != -1) data[itemOffset + 15] = (byte) newData;

        System.out.print("New Type (0 = Block, 1 = Item, -1 to keep): ");
        int newType = scanner.nextInt();
        if (newType != -1) data[itemOffset + 23] = (byte) newType;

        try {
            Files.write(Path.of(filePath), data);
            
            // Show updated name
            if (newID != -1 || newData != -1) {
                int updatedID = (newID != -1) ? newID : unsignedID;
                int updatedData = (newData != -1) ? newData : unsignedData;
                int updatedType = (newType != -1) ? newType : oldIsItem;
                String updatedName = getName(updatedID, updatedData, updatedType == 1);
                System.out.printf("%s updated successfully! New name: %s%n", name[index], updatedName);
            } else {
                System.out.println(name[index] + " updated successfully!");
            }
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    public static void editItem(Scanner scanner) {
        int startOffset = 716 + nameLength;

        System.out.print("Enter item index (0 to " + (itemCount - 1) + "): ");
        int index = scanner.nextInt();

        if (index < 0 || index >= itemCount) {
            System.out.println("Invalid index!");
            return;
        }

        int itemOffset = startOffset + (index * 36);

        byte oldCount = data[itemOffset + 5];
        byte oldData = data[itemOffset + 15];
        byte oldID = data[itemOffset + 30];
        byte oldIsItem = data[itemOffset + 31];

        int unsignedID = oldID & 0xFF;
        int unsignedData = oldData & 0xFF;
        int unsignedCount = oldCount & 0xFF;
        String oldName = getName(unsignedID, unsignedData, oldIsItem == 1);
        
        System.out.printf("Editing item:%nCurrent -> ID: %d | Count: %d | Data: %d | Type: %s | Name: %s%n",
                unsignedID, unsignedCount, unsignedData, 
                (oldIsItem == 1 ? "Item" : "Block"), oldName);

        System.out.print("New ID (or -1 to keep): ");
        int newID = scanner.nextInt();
        if (newID != -1) data[itemOffset + 30] = (byte) newID;

        System.out.print("New Count (or -1 to keep): ");
        int newCount = scanner.nextInt();
        if (newCount != -1) data[itemOffset + 5] = (byte) newCount;

        System.out.print("New Data Value (or -1 to keep): ");
        int newData = scanner.nextInt();
        if (newData != -1) data[itemOffset + 15] = (byte) newData;

        System.out.print("New Type (0 = Block, 1 = Item, -1 to keep): ");
        int newType = scanner.nextInt();
        if (newType != -1) data[itemOffset + 31] = (byte) newType;

        try {
            Files.write(Path.of(filePath), data);
            
            // Show updated name
            if (newID != -1 || newData != -1) {
                int updatedID = (newID != -1) ? newID : unsignedID;
                int updatedData = (newData != -1) ? newData : unsignedData;
                int updatedType = (newType != -1) ? newType : oldIsItem;
                String updatedName = getName(updatedID, updatedData, updatedType == 1);
                System.out.printf("Item updated! New name: %s%n", updatedName);
            } else {
                System.out.println("Item updated!");
            }
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    public static void addItem(Scanner scanner) {
        try {
            System.out.print("Are you adding a block or an item? (0 = Block, 1 = Item): ");
            int isItem = scanner.nextInt();

            System.out.print("What are you adding? (0-255): ");
            int itemID = scanner.nextInt();

            System.out.print("What data value? (0?): ");
            int dataValue = scanner.nextInt();

            System.out.print("How many? (0-255): ");
            int count = scanner.nextInt();

            // Show the name of what they're adding
            String itemName = getName(itemID, dataValue, isItem == 1);
            System.out.printf("Adding: %s (ID: %d, Data: %d)%n", itemName, itemID, dataValue);

            int insertPos = 716 + nameLength + (itemCount * 36);

            int startOffset = 716 + nameLength;
            boolean[] usedSlots = new boolean[36];

            // Mark used slots
            for (int i = 0; i < itemCount; i++) {
                int itemOffset = startOffset + (i * 36);
                int slotNum = (data[itemOffset + 24] & 0xFF) - 9; // convert back to 0–35
                if (slotNum >= 0 && slotNum < 36) {
                    usedSlots[slotNum] = true;
                }
            }

            // Find lowest available slot
            int nextSlot = -1;
            for (int i = 0; i < 36; i++) {
                if (!usedSlots[i]) {
                    nextSlot = i;
                    break;
                }
            }
            
            // Insert the data with the required bytes around it
            byte[] insert = {
                    0x43, 0x6F, 0x75, 0x6E, 0x74, (byte) count, 0x02, 0x06, 0x00, //Count
                    0x44, 0x61, 0x6D, 0x61, 0x67, 0x65, (byte) dataValue, 0x00, 0x01, 0x04, 0x00, //Damage/Data
                    0x53, 0x6C, 0x6F, 0x74, (byte) (nextSlot + 9), 0x02, 0x02, 0x00, //Slot
                    0x69, 0x64, (byte) itemID, (byte) isItem, 0x00, 0x09, 0x06, 0x00 // ID
            };

            byte[] newData = new byte[data.length + insert.length];

            System.arraycopy(data, 0, newData, 0, insertPos);
            System.arraycopy(insert, 0, newData, insertPos, insert.length);
            System.arraycopy(data, insertPos, newData, insertPos + insert.length, data.length - insertPos);

            // Change previous items data so it doesn't think that is the last one in the list
            newData[insertPos - 3] = 0x01;
            newData[insertPos - 2] = 0x05;

            itemCount++;
            updateInventoryMetadata(newData);

            Files.write(Path.of(filePath), newData);
            System.out.println("Item added successfully!");

        } catch (IOException e) {
            System.out.println("Error adding item: " + e.getMessage());
        }
    }

    // -----------------------------
    //    FILE STRUCTURE UPDATES
    // -----------------------------

    public static void updateInventoryMetadata(byte[] newData) {
        // Make sure the checksum at the top of the file is 8 bytes less than the length of the file
        int checksumCount = newData.length - 8;
        newData[4] = (byte) (checksumCount & 0xFF);
        newData[5] = (byte) ((checksumCount >> 8) & 0xFF);

        // Update the count of how many items are in the inventory
        int inventoryCount = Math.max(9, itemCount + 9);
        newData[385 + nameLength] = (byte) (inventoryCount & 0xFF);

        System.out.printf("Updated metadata -> Checksum: %d | Inventory count: %d%n",
                checksumCount, inventoryCount);
    }

    // -----------------------------
    //        HOTBAR CLEANUP
    // -----------------------------

    public static void removeHotbar() {
        // This method is just here to prevent crashes and headaches
        int[] hotbarValues = {
                407, 408, 443, 444, 479, 480, 515, 516,
                551, 552, 587, 588, 623, 624, 659, 660,
                695, 696
        };

        for (int offset : hotbarValues) {
            data[offset + nameLength] = -1;
        }
    }

	// -----------------------------
    //     CSV LOADING METHODS
    // -----------------------------

    public static void loadBlockCSV() throws IOException {
        Path blocksPath = Path.of("Blocks.csv");
        if (!Files.exists(blocksPath)) {
            System.out.println("Warning: Blocks.csv not found. Names will not be displayed.");
            return;
        }
        
        String content = Files.readString(blocksPath);
        String[] lines = content.split("\n");
        
        // Skip header line
        for (int i = 1; i < lines.length; i++) {
            String[] parts = lines[i].split(",", -1); // -1 to keep trailing empty strings
            if (parts.length >= 3) {
                try {
                    int blockID = Integer.parseInt(parts[0].trim());
                    int dataValue = Integer.parseInt(parts[1].trim());
                    String name = parts[2].trim();
                    
                    String key = blockID + ":" + dataValue;
                    blockNames.put(key, name);
                    
                    // Also store without data value for default lookup
                    if (dataValue == 0) {
                        blockNames.put(String.valueOf(blockID), name);
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid lines
                }
            }
        }
        System.out.println("Loaded " + blockNames.size() + " block names");
    }

    public static void loadItemCSV() throws IOException {
        Path itemsPath = Path.of("Items.csv");
        if (!Files.exists(itemsPath)) {
            System.out.println("Warning: Items.csv not found. Names will not be displayed.");
            return;
        }
        
        String content = Files.readString(itemsPath);
        String[] lines = content.split("\n");
        
        // Skip header line
        for (int i = 1; i < lines.length; i++) {
            String[] parts = lines[i].split(",", -1); // -1 to keep trailing empty strings
            if (parts.length >= 3) {
                try {
                    int itemID = Integer.parseInt(parts[0].trim());
                    int dataValue = Integer.parseInt(parts[1].trim());
                    String name = parts[2].trim();
                    
                    String key = itemID + ":" + dataValue;
                    itemNames.put(key, name);
                    
                    // Also store without data value for default lookup
                    if (dataValue == 0) {
                        itemNames.put(String.valueOf(itemID), name);
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid lines
                }
            }
        }
        System.out.println("Loaded " + itemNames.size() + " item names");
    }

    // -----------------------------
    //     NAME LOOKUP METHODS
    // -----------------------------

    public static String getName(int id, int dataValue, boolean isItem) {
        String key = id + ":" + dataValue;
        
        if (isItem) {
            String name = itemNames.get(key);
            if (name != null) return name;
            
            // Fallback to data value 0
            name = itemNames.get(String.valueOf(id));
            return name != null ? name : "Unknown Item";
        } else {
            String name = blockNames.get(key);
            if (name != null) return name;
            
            // Fallback to data value 0
            name = blockNames.get(String.valueOf(id));
            return name != null ? name : "Air";
        }
    }
}
