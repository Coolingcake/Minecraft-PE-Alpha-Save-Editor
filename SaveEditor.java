import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Arrays;

public class SaveEditor {
    public static int itemCount;
    public static byte[] data;
    public static int nameLength;
    public static String filePath;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // File path input
            System.out.print("Enter file path: ");
            filePath = scanner.nextLine();
            

            // Read file
            data = Files.readAllBytes(Path.of(filePath));
            System.out.println("File loaded! Size: " + data.length + " bytes.");

            // Extract metadata
            nameLength = data[59];
            System.out.println("World name length: " + nameLength);

            itemCount = data[385 + nameLength] - 9;
            System.out.println("Item Count: " + itemCount);

            // Remove hotbar references
            removeHotbar();

            // Show and modify inventory
			displayArmor(scanner);
			if (itemCount > 0) {
            	displayInventory(scanner);
			}
			if (itemCount < 36) {
            	addItem(scanner);
			}

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

        scanner.close();
    }

    // -----------------------------
    // INVENTORY MANAGEMENT
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

			System.out.printf("[%s] ID: %d | Count: %d | Data: %d | Type: %s%n",
                    name[i], id & 0xFF, count & 0xFF, dataValue & 0xFF, (isItem == 1 ? "Item" : "Block"));

		}

		System.out.println("-----------------------");

		System.out.print("Do you want to edit your armor? (y/n): ");
        if (scanner.next().equalsIgnoreCase("y")) {
			editArmor(scanner);
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

            System.out.printf("[%d] ID: %d | Count: %d | Data: %d | Type: %s%n",
                    i, id & 0xFF, count & 0xFF, dataValue & 0xFF, (isItem == 1 ? "Item" : "Block"));
        }

        System.out.println("-----------------------");

        System.out.print("Do you want to edit an item? (y/n): ");
        if (scanner.next().equalsIgnoreCase("y")) {
			editItem(scanner);
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

		System.out.printf("Editing %s:%nCurrent -> ID: %d | Count: %d | Data: %d | Type: %s%n",
				name[index], oldID & 0xFF, oldCount & 0xFF, oldData & 0xFF,
				(oldIsItem == 1 ? "Item" : "Block"));

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
			System.out.println(name[index] + " updated successfully!");
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

        System.out.printf("Editing item:%nCurrent -> ID: %d | Count: %d | Data: %d | Type: %s%n",
                oldID & 0xFF, oldCount & 0xFF, oldData & 0xFF, (oldIsItem == 1 ? "Item" : "Block"));

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
            System.out.println("Item updated!");
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    public static void addItem(Scanner scanner) {
		System.out.print("Do you want to add an item? (y/n): ");
        if (scanner.next().equalsIgnoreCase("n")) {
        	return;
        }
        try {
			System.out.print("Are you adding a block or an item? (0 = Block, 1 = Item): ");
            int isItem = scanner.nextInt();

            System.out.print("What are you adding? (0-255): ");
            int itemID = scanner.nextInt();

            System.out.print("What data value? (0?): ");
            int dataValue = scanner.nextInt();

            System.out.print("How many? (0-255): ");
            int count = scanner.nextInt();

            int insertPos = 716 + nameLength + (itemCount * 36);

            byte[] insert = {
                    0x43, 0x6F, 0x75, 0x6E, 0x74, (byte) count, 0x02, 0x06, 0x00, //Count
                    0x44, 0x61, 0x6D, 0x61, 0x67, 0x65, (byte) dataValue, 0x00, 0x01, 0x04, 0x00, //Damage/Data
                    0x53, 0x6C, 0x6F, 0x74, (byte) (itemCount + 9), 0x02, 0x02, 0x00, //Slot
                    0x69, 0x64, (byte) itemID, (byte) isItem, 0x00, 0x09, 0x06, 0x00 // ID
            };

            byte[] newData = new byte[data.length + insert.length];

            System.arraycopy(data, 0, newData, 0, insertPos);
            System.arraycopy(insert, 0, newData, insertPos, insert.length);
            System.arraycopy(data, insertPos, newData, insertPos + insert.length, data.length - insertPos);

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
    // FILE STRUCTURE UPDATES
    // -----------------------------

    public static void saveChecksum() {
        int checksumCount = data.length - 8;
        data[4] = (byte) (checksumCount & 0xFF);
        data[5] = (byte) ((checksumCount >> 8) & 0xFF);
        System.out.println("Checksum updated: " + checksumCount);
    }

    public static void saveInventoryCount() {
        int inventoryCount = Math.max(9, itemCount + 9);
        data[385 + nameLength] = (byte) (inventoryCount & 0xFF);
        System.out.println("Inventory count updated: " + inventoryCount);
    }

    public static void updateInventoryMetadata(byte[] newData) {
        int checksumCount = newData.length - 8;
        newData[4] = (byte) (checksumCount & 0xFF);
        newData[5] = (byte) ((checksumCount >> 8) & 0xFF);

        int inventoryCount = Math.max(9, itemCount + 9);
        newData[385 + nameLength] = (byte) (inventoryCount & 0xFF);

        System.out.printf("Updated metadata -> Checksum: %d | Inventory count: %d%n",
                checksumCount, inventoryCount);
    }

    // -----------------------------
    // HOTBAR CLEANUP
    // -----------------------------

    public static void removeHotbar() {
        int[] hotbarValues = {
                407, 408, 443, 444, 479, 480, 515, 516,
                551, 552, 587, 588, 623, 624, 659, 660,
                695, 696
        };

        for (int offset : hotbarValues) {
            data[offset + nameLength] = -1;
        }
    }
}
