package nl.mightydev.version;

public class LumberjackVersion {
	public final String value;
	
	public LumberjackVersion(String version) {
		value = version;
	}

	public String toString() {
		return value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LumberjackVersion other = (LumberjackVersion) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
