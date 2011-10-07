/* Created on Oct 11, 2010 */
package edu.unl.cig.aurora.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author Ian Cottingham
 * 
 */
public class PlaceCollectionBuilder {

	private static final DateTimeFormatter FORMAT = DateTimeFormat
			.forPattern("yyyy-MM-dd");

	private ArrayList<DateTime> dates;
	private HashMap<String, Set<PlaceReference>> placeReferences;

	public PlaceCollectionBuilder() {
		this.placeReferences = new HashMap<String, Set<PlaceReference>>();
		this.dates = new ArrayList<DateTime>();
	}

	public void addPlaceReference(PlaceReference ref) {

		if (!dates.contains(ref.getReferenceDate())) {
			dates.add(ref.getReferenceDate());
		}

		if (placeReferences.containsKey(ref.getPlaceKey())) {
			Set<PlaceReference> refs = placeReferences.get(ref.getPlaceKey());

			if (refs.contains(ref)) {
				ref.extractFromSet(refs)
						.incrementCount(ref.getReferenceCount());
			} else {
				refs.add(ref);
			}
		} else {
			TreeSet<PlaceReference> set = new TreeSet<PlaceReference>();
			set.add(ref);
			placeReferences.put(ref.getPlaceKey(), set);
		}
	}

	public PlaceCollection getRolledUpCollection() {
		if (this.dates.size() > 300) {
			return getRolledUpCollection(RollupRule.YEARLY);
		} else if (this.dates.size() > 100) {
			return getRolledUpCollection(RollupRule.MONTHLY);
		} else if (this.dates.size() > 20) {
			return getRolledUpCollection(RollupRule.WEEKLY);
		} else {
			return getCollection();
		}
	}

	public PlaceCollection getRolledUpCollection(RollupRule rule) {
		ArrayList<DateTime> rolled_dates = new ArrayList<DateTime>();
		HashMap<String, Set<PlaceReference>> rolled_references = new HashMap<String, Set<PlaceReference>>();

		for (DateTime date : this.dates) {
			date = rollDate(rule, date);
			if (!rolled_dates.contains(date)) {
				rolled_dates.add(date);
			}
		}

		PlaceReference new_ref;
		for (String key : placeReferences.keySet()) {
			Set<PlaceReference> set = new TreeSet<PlaceReference>();

			for (PlaceReference ref : placeReferences.get(key)) {
				new_ref = ref.copyWithNewDate(rollDate(rule,
						ref.getReferenceDate()));
				
				if (set.contains(new_ref)) {
					new_ref.extractFromSet(set).incrementCount(
							ref.getReferenceCount());
				} else {
					set.add(new_ref);
				}
			}

			rolled_references.put(key, set);
		}

		return buildCollection(rolled_dates, rolled_references);
	}

	public PlaceCollection getCollection() {
		return buildCollection(this.dates, this.placeReferences);
	}

	private PlaceCollection buildCollection(ArrayList<DateTime> dates,
			HashMap<String, Set<PlaceReference>> placeReferences) {
		PlaceCollection collection = new PlaceCollection();
		collection.setDates(dates, FORMAT);

		int[] base_array = new int[dates.size()];

		ArrayList<PlaceReference> flattened_references = new ArrayList<PlaceReference>();
		int[] place_counts;
		PlaceReference place;

		for (String place_key : placeReferences.keySet()) {
			place_counts = base_array.clone();
			place = null;
			for (PlaceReference ref_obj : placeReferences.get(place_key)) {
				if (place == null) {
					place = new PlaceReference(ref_obj.getPlaceName(),
							ref_obj.getActualText(), 0,
							ref_obj.getReferenceDate(), ref_obj.getPlaceKey(),
							ref_obj.getLatitude(), ref_obj.getLongitude());
				}

				place.incrementCount(ref_obj.getReferenceCount());
				place_counts[dates.indexOf(ref_obj.getReferenceDate())] = ref_obj
						.getReferenceCount();
			}

			place.setReferenceCounts(place_counts);
			flattened_references.add(place);
		}

		collection.setReferences(flattened_references);
		return collection;
	}

	private DateTime rollDate(RollupRule rule, DateTime date) {
		switch (rule) {
		case WEEKLY:
			return date.minusDays(date.getDayOfWeek()); // round to the first
														// day of the week
		case MONTHLY:
			return new DateTime(date.getYear(), date.getMonthOfYear(), 1, 0, 0,
					0, 0, GregorianChronology.getInstance()); // round to first
																// day of the
																// month
		case YEARLY:
			return new DateTime(date.getYear(), 1, 1, 0, 0, 0, 0,
					GregorianChronology.getInstance()); // round to first day of the year
		case SINGLE_VALUE:										
			return dates.get(0);
		default:
			return date;
		}
	}

	public enum RollupRule {
		WEEKLY, MONTHLY, YEARLY, SINGLE_VALUE;
	}
}
