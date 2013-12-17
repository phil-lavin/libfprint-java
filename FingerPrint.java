import jlibfprint.*;
import java.io.*;
import java.util.*;

public class FingerPrint {
	protected static HashMap<String, JlibFprint.fp_print_data> _print_data;

	public static void syntax() {
		System.out.println("Syntax:");
		System.out.println();
		System.out.println("FingerPrint enroll <name> - Enrolls the person named");
		System.out.println("FingerPrint verify - Verifies a print and tells you who it belongs to");
		System.exit(1);
	}

	public static void main(String[] args) {
		if (args.length < 1)
			syntax();

		_print_data = read_from_files();

		if (args[0].equals("enroll") && args.length < 2)
			syntax();
		else if (args[0].equals("enroll"))
			enroll(args[1]);

		if (args[0].equals("verify"))
			verify((args.length > 1));
	}

	public static JlibFprint.fp_print_data get_print(String prefix, boolean repeat) {
		JlibFprint jlibfprint = new JlibFprint();
		boolean broked = false;
		JlibFprint.fp_print_data print11 = null;
		JlibFprint.fp_print_data print12 = null;

		do {
			if (broked)
				System.err.println("Sorry, they didn't match. Try again!\n");

			try {
				System.out.println(prefix+" print please");
				print11 = jlibfprint.enroll_finger();

				if (repeat) {
					System.out.println(prefix+" print again please");
					print12 = jlibfprint.enroll_finger();

					broked = (JlibFprint.img_compare_print_data(print11, print12) < JlibFprint.BOZORTH_THRESHOLD);
				}
				else {
					broked = false;
				}
			}
			catch (JlibFprint.EnrollException e) {
				broked = true;
			}
		} while (broked);

		return print11;
	}

	public static void enroll(String name) {
		JlibFprint.fp_print_data print1 = get_print("First", true);
		JlibFprint.fp_print_data print2 = get_print("Second", true);

		serialize_to_file(name+"-1", print1);
		serialize_to_file(name+"-2", print2);
	}

	public static void verify(boolean debug) {
		// Get a print to compare
		JlibFprint.fp_print_data fpd = get_print("Your", false);

		if (fpd != null) {
			// Loop HashMap and compare with each print
			Iterator it = _print_data.entrySet().iterator();

			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();

				JlibFprint.fp_print_data compare = (JlibFprint.fp_print_data)entry.getValue();

				int matchValue = JlibFprint.img_compare_print_data(fpd, compare);

				if (debug) {
					System.out.println("Print compares to "+entry.getKey()+" with a Bozorth value of "+matchValue+" (threshold = "+JlibFprint.BOZORTH_THRESHOLD+")");
				}

				if (matchValue > JlibFprint.BOZORTH_THRESHOLD) {
					String name = (String)entry.getKey();
					String[] name_split = name.split("-");

					System.out.println("This print belongs to " + name_split[0]);
					return;
				}
			}
		}

		System.out.println("No match found");
	}

	public static void serialize_to_file(String name, JlibFprint.fp_print_data fpd) {
		try {
			FileOutputStream fout = new FileOutputStream("prints/"+name+".ser");
			ObjectOutputStream oos = new ObjectOutputStream(fout);

			oos.writeObject(fpd);
		}
		catch (IOException e) {
			System.err.println("Failed to write to prints/"+name+".ser");
		}
	}

	public static HashMap<String, JlibFprint.fp_print_data> read_from_files() {
		HashMap<String, JlibFprint.fp_print_data> rtn = new HashMap<String, JlibFprint.fp_print_data>();

		File dir = new File("prints");
		for (File child : dir.listFiles()) {
			// Skip dot files
			if (child.getName().charAt(0) == '.') {
				continue;
			}

			try {
				// Get the name from the file
				String name = child.getName().split("\\.")[0];

				// Get the object from the file
				InputStream file = new FileInputStream("prints/"+child.getName());
				InputStream buffer = new BufferedInputStream(file);
				ObjectInput input = new ObjectInputStream(buffer);

				JlibFprint.fp_print_data data = (JlibFprint.fp_print_data)input.readObject();

				rtn.put(name, data);
			}
			catch(ClassNotFoundException e) {
				System.err.println("Failed to deserialize as class wasn't found");
			}
			catch(IOException e) {
				System.err.println("Failed to read file prints/"+child.getName());
			}
		}

		return rtn;
	}
}
