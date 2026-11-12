package me.aap.fermata.ui.fragment;

/** Responsive dashboard spans: four equal cards on wide reference layouts. */
final class DashboardGridPolicy {
	private DashboardGridPolicy() {
	}

	static int gridSpans(int columns) {
		if (columns >= 4) return 20;
		return columns >= 3 ? 15 : Math.max(1, columns);
	}

	static int itemSpan(boolean featured, boolean wide, int spans) {
		if (wide) return spans;
		if (spans == 20) return 5;
		if (spans == 15) return featured ? 5 : 3;
		return 1;
	}
}
