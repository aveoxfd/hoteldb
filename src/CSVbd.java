import java.util.List;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class CSVbd {
	public static void save(List<HotelList> hotels, String filePath) throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("ID,HotelName,City,AddressName,StreetNumber,HouseNumber,DoorNumber,");
		sb.append("AdminFirst,AdminSecond,AdminMiddle,AdminPhone,AdminPost,");
		sb.append("DirectorFirst,DirectorSecond,DirectorMiddle,DirectorPhone,DirectorPost");
		sb.append(System.lineSeparator());

		for (HotelList h : hotels) {
			if (h == null) continue;
			String id = safe(h.ID);
			String hotelName = safe(h.hotelName);
			String city = h.hotelAddress != null ? safe(h.hotelAddress.cityName) : "null";
			String addrName = h.hotelAddress != null ? safe(h.hotelAddress.addressName) : "null";
			String streetNumber = h.hotelAddress != null ? String.valueOf(h.hotelAddress.streetNumber) : "0";
			String houseNumber = h.hotelAddress != null ? String.valueOf(h.hotelAddress.houseNumber) : "0";
			String doorNumber = h.hotelAddress != null ? String.valueOf(h.hotelAddress.doorNumber) : "0";

			Person a = h.administator;
			Person d = h.director;
			String aFirst = a != null ? safe(a.firstName) : "null";
			String aSecond = a != null ? safe(a.secondName) : "null";
			String aMiddle = a != null ? safe(a.middleName) : "null";
			String aPhone = a != null ? safe(a.phoneNumber) : "null";
			String aPost = a != null ? safe(a.post) : "null";

			String dFirst = d != null ? safe(d.firstName) : "null";
			String dSecond = d != null ? safe(d.secondName) : "null";
			String dMiddle = d != null ? safe(d.middleName) : "null";
			String dPhone = d != null ? safe(d.phoneNumber) : "null";
			String dPost = d != null ? safe(d.post) : "null";

			sb.append(quote(id)).append(',')
			  .append(quote(hotelName)).append(',')
			  .append(quote(city)).append(',')
			  .append(quote(addrName)).append(',')
			  .append(quote(streetNumber)).append(',')
			  .append(quote(houseNumber)).append(',')
			  .append(quote(doorNumber)).append(',')
			  .append(quote(aFirst)).append(',')
			  .append(quote(aSecond)).append(',')
			  .append(quote(aMiddle)).append(',')
			  .append(quote(aPhone)).append(',')
			  .append(quote(aPost)).append(',')
			  .append(quote(dFirst)).append(',')
			  .append(quote(dSecond)).append(',')
			  .append(quote(dMiddle)).append(',')
			  .append(quote(dPhone)).append(',')
			  .append(quote(dPost))
			  .append(System.lineSeparator());
		}

		Files.write(Paths.get(filePath), sb.toString().getBytes(StandardCharsets.UTF_8));
	}

	public static List<HotelList> load(String filePath) throws IOException {
		List<HotelList> out = new ArrayList<>();
		java.nio.file.Path p = Paths.get(filePath);
		if (!Files.exists(p)) return out;
		List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
		if (lines.size() <= 1) return out; // no data
		// skip header (first line)
		for (int i = 1; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.trim().isEmpty()) continue;
			List<String> cols = parseCsvLine(line);
			// expect 16 columns as written by save()
			while (cols.size() < 16) cols.add("");
			HotelList h = new HotelList();
			h.ID = emptyToNull(cols.get(0));
			h.hotelName = emptyToNull(cols.get(1));
			h.hotelAddress = new Address();
			h.hotelAddress.cityName = emptyToNull(cols.get(2));
			h.hotelAddress.addressName = emptyToNull(cols.get(3));
			String sn = cols.get(4);
			String hn = cols.get(5);
			String dn = cols.get(6);
			try { h.hotelAddress.streetNumber = sn.isEmpty() ? 0 : Integer.parseInt(sn); } catch(Exception ex){ h.hotelAddress.streetNumber = 0; }
			try { h.hotelAddress.houseNumber = hn.isEmpty() ? 0 : Integer.parseInt(hn); } catch(Exception ex){ h.hotelAddress.houseNumber = 0; }
			try { h.hotelAddress.doorNumber = dn.isEmpty() ? 0 : Integer.parseInt(dn); } catch(Exception ex){ h.hotelAddress.doorNumber = 0; }

			Person a = new Person();
			a.firstName = emptyToNull(cols.get(7));
			a.secondName = emptyToNull(cols.get(8));
			a.middleName = emptyToNull(cols.get(9));
			a.phoneNumber = emptyToNull(cols.get(10));
			a.post = emptyToNull(cols.get(11));
			h.administator = hasPersonData(a) ? a : null;

			Person d = new Person();
			d.firstName = emptyToNull(cols.get(12));
			d.secondName = emptyToNull(cols.get(13));
			d.middleName = emptyToNull(cols.get(14));
			d.phoneNumber = emptyToNull(cols.get(15));
			d.post = emptyToNull(cols.get(16)); 
			h.director = hasPersonData(d) ? d : null;

			out.add(h);
		}
		return out;
	}

	private static boolean hasPersonData(Person p) {
		return (p.firstName != null && !p.firstName.isEmpty()) || (p.secondName != null && !p.secondName.isEmpty()) || (p.phoneNumber != null && !p.phoneNumber.isEmpty()) || (p.post != null && !p.post.isEmpty()) || (p.middleName != null && !p.middleName.isEmpty());
	}

	private static String emptyToNull(String s) {
		if (s == null) return null;
		String t = s.trim();
		return t.isEmpty() ? null : t;
	}

	private static List<String> parseCsvLine(String line) {
		List<String> cols = new ArrayList<>();
		StringBuilder cur = new StringBuilder();
		boolean inQuotes = false;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (inQuotes) {
				if (c == '"') {
					// look ahead for escaped quote
					if (i + 1 < line.length() && line.charAt(i+1) == '"') {
						cur.append('"');
						i++; // skip next
					} else {
						inQuotes = false;
					}
				} else {
					cur.append(c);
				}
			} else {
				if (c == ',') {
					cols.add(cur.toString());
					cur.setLength(0);
				} else if (c == '"') {
					inQuotes = true;
				} else {
					cur.append(c);
				}
			}
		}
		cols.add(cur.toString());
		return cols;
	}

	private static String safe(String s) {
		return s == null ? "" : s;
	}

	private static String quote(String s) {
		String escaped = s.replace("\"", "\"\"");
		return "\"" + escaped + "\"";
	}
}
