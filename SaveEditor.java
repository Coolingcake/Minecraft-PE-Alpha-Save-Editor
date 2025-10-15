import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Arrays;

public class SaveEditor {
	public static int itemCount;
	public static byte[] data;
	public static int nameLength;
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Ask for file path
            System.out.print("Enter file path: ");
            String filePath = scanner.nextLine();

            // Read file bytes
            data = Files.readAllBytes(Path.of(filePath));
            System.out.println("File loaded! Size: " + data.length + " bytes.");

			//Get world name length for offsets
			nameLength = data[59];
			System.out.println("World name length: " + nameLength);

			//Get number of items in inventory
			itemCount = data[385 + nameLength] - 9;
			System.out.println("Item Count: " + itemCount);
			
			removeHotbar();
			addItem(scanner, filePath);

			//Save file
			//saveInventoryCount();
			//saveChecksum();
			//Files.write(Path.of(filePath), data);

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

		scanner.close();
    }

	public static void removeHotbar() {
		int[] hotbarValues = {
			407, 408, 443, 444, 479, 480, 515, 516, 551, 552, 587, 588, 623, 624, 659, 660, 695, 696
		};

		for (int i = 0; i < hotbarValues.length; i++) {
			data[hotbarValues[i] + nameLength] = -1;
		}
	}

	public static void addItem(Scanner scanner, String filePath) throws IOException {
		System.out.println("What are you adding? (0-255) ");
		int itemID = scanner.nextInt();
		System.out.println("Is it a block or an item (0 or 1) ");
		int isItem = scanner.nextInt();
		System.out.println("What data value? (0?) ");
		int dataValue = scanner.nextInt();
		System.out.println("How many? (0-255) ");
		int count = scanner.nextInt();

		int insertPos = 716 + nameLength + (itemCount * 36);

		byte[] insert = {
			0x43, 0x6F, 0x75, 0x6E, 0x74, (byte)count, 0x02, 0x06, 0x00, 0x44, 0x61, 0x6D, 0x61, 0x67, 0x65, (byte)dataValue, 0x00, 0x01, 0x04, 0x00, 0x53, 0x6C, 0x6F, 0x74, (byte)(itemCount+9), 0x02, 0x02, 0x00, 0x69, 0x64, (byte)itemID, (byte)isItem, 0x00, 0x09, 0x06, 0x00
		};

		byte[] newData = new byte[data.length + insert.length];

		System.arraycopy(data, 0, newData, 0, insertPos);

		System.arraycopy(insert, 0, newData, insertPos, insert.length);

		System.arraycopy(data, insertPos, newData, insertPos + insert.length, data.length - insertPos);

		newData[insertPos-3] = 0x01;
		newData[insertPos-2] = 0x05;

		itemCount++;

		saveNewDataChecksum(newData);
		saveNewDataInventoryCount(newData);
		Files.write(Path.of(filePath), newData);
	}

	public static void saveChecksum() {
		int checksumCount = data.length - 8;
		data[4] = (byte) (checksumCount & 0xFF);
		data[5] = (byte) ((checksumCount >> 8) & 0xFF);

		System.out.println("Checksum value: " + checksumCount);
	}

	public static void saveInventoryCount() {
		int inventoryCount = itemCount + 9;
		if (inventoryCount < 9) {
			inventoryCount = 9;
		}
		data[385 + nameLength] = (byte) (inventoryCount & 0xFF);

		System.out.println("Inventory value: " + inventoryCount);
	}

	public static void saveNewDataChecksum(byte[] newData) {
		int checksumCount = newData.length - 8;
		newData[4] = (byte) (checksumCount & 0xFF);
		newData[5] = (byte) ((checksumCount >> 8) & 0xFF);

		System.out.println("Checksum value: " + checksumCount);
	}

	public static void saveNewDataInventoryCount(byte[] newData) {
		int inventoryCount = itemCount + 9;
		if (inventoryCount < 9) {
			inventoryCount = 9;
		}
		newData[385 + nameLength] = (byte) (inventoryCount & 0xFF);

		System.out.println("Inventory value: " + inventoryCount);
	}
}

