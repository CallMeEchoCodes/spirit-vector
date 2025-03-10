package symbolics.division.spirit_vector.logic.spell;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class EidosEdge {
	public int up = 1;
	public int down = 0;
	private int off = 0;
	public Vector3f anchor;
	public Vector3fc normal;

	public EidosEdge(int x, int z, int nx, int nz) {
		this.anchor = new Vector3f(x, 0, z);
		this.normal = new Vector3f(nx, 0, nz);
	}

	public boolean inForwardPlane(Vector3fc p) {
		// true if touching or past plane in direction of normal
		return p.sub(anchor, new Vector3f()).dot(normal) >= 0;
	}

	public void extrudeForward(Set<Vector3fc> eidos) {
		List<Vector3fc> toAdd = new ArrayList<>();
		for (var p : eidos) {
			if (inForwardPlane(p)) {
				toAdd.add(p.add(normal, new Vector3f()));
			}
		}
		eidos.addAll(toAdd);
		anchor.add(normal);
		off++;
	}

	public void extrudeUp(Set<Vector3fc> eidos) {
		List<Vector3fc> toAdd = new ArrayList<>();
		for (var p : eidos) {
			if (inForwardPlane(p) && p.y() == up) {
				toAdd.add(p.add(0, 1, 0, new Vector3f()));
			}
		}
		eidos.addAll(toAdd);
		up += 1;
	}

	public void extrudeDown(Set<Vector3fc> eidos) {
		List<Vector3fc> toAdd = new ArrayList<>();
		for (var p : eidos) {
			if (inForwardPlane(p) && p.y() == down) {
				toAdd.add(p.add(0, -1, 0, new Vector3f()));
			}
		}
		eidos.addAll(toAdd);
		down -= 1;
	}

	public int size() {
		return Math.max(up, Math.max(Math.abs(down), Math.abs(off)));
	}

}
