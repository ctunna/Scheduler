package taAllocation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class TAallocation extends PredicateReader implements
		TAallocationPredicates {

	protected long max_labs = Long.MAX_VALUE;
	protected long min_labs = 0;
	protected HashMap<String, TA> tas = new HashMap<String, TA>();
	protected HashMap<String, Instructor> instructors = new HashMap<String, Instructor>();
	protected HashMap<String, Course> courses = new HashMap<String, Course>();
	protected HashMap<String, Timeslot> timeslots = new HashMap<String, Timeslot>();
	static final int DEFAULT_MAX_TIME = 30000;
	static PrintStream traceFile;

	

	public static void main(String[] args) {
		
		TAallocation env = new TAallocation("TAallocation");
		String infile = "input.txt";
		String outfile = "trace.out";
		boolean consoleMode = false;
		
		// READ FROM FILE
		if (args.length > 0)
		{
			infile = args[0];
			outfile = infile + ".out";
			
		} else {
			infile = "input.txt";
			outfile = "traced.out";
			consoleMode = true;
		}
		
		env.fromFile(infile);
		try {
			traceFile = new PrintStream(new FileOutputStream(outfile));
			//traceFile.print("Trace taAllocation.Test");
			//for (String s : args)
			//	traceFile.print(" " + s);
			//traceFile.println("\n" + new java.util.Date());
		} catch (Exception ex) {
			traceFile = null;
		}
		
		//printSynopsis();

		if (consoleMode) commandMode(env);
		env.a_show("");

		if (traceFile != null) {
			//traceFile.println(new java.util.Date());
			traceFile.close();
		}
	}

	/**
	 * Implement "command mode": repeatedly read lines of predicates from
	 * {@link System#in} and let PredicateReaderher assert them (if the line
	 * starts with a "!") or evaluate them (and return "true" or "false" to
	 * {@link System#out}.
	 * 
	 * @param env
	 *            the environment that can interpret the predicates.
	 */
	public static void commandMode(PredicateReader env) {
		final int maxBuf = 200;
		byte[] buf = new byte[maxBuf];
		int length;
		try {
			print("\nSisyphus I: query using predicates, assert using \"!\" prefixing predicates;\n !exit() to quit; !help() for help.\n\n> ");
			while ((length = System.in.read(buf)) != -1) {
				String s = new String(buf, 0, length);
				s = s.trim();
				if (s.equals("exit"))
					break;
				if (s.equals("?") || s.equals("help")) {
					s = "!help()";
					println("> !help()");
				}
				if (s.length() > 0) {
					if (s.charAt(0) == '!') {
						env.assert_(s.substring(1));
					} else {
						print(" --> " + env.eval(s));
					}
				}
				print("\n> ");
			}

		} catch (Exception e) {
			e.printStackTrace();
			println("exiting: " + e.toString());
		}
	}

	static void printSynopsis() {
		println("Synopsis: Sisyphus <search-prg> [<env-file> [<maxTimeInMilliseconds:default="
				+ DEFAULT_MAX_TIME + ">]]");
	}

	static void println(String s) {
		System.out.println(s);
		traceFile.println(s);
	}

	static void print(String s) {
		System.out.print(s);
		traceFile.print(s);
	}

	static void write(byte[] s, int offset, int count) throws Exception {
		System.out.write(s, offset, count);
		traceFile.write(s, offset, count);
		;
	}

	public TAallocation(String string) {
		super(string);
		// TODO Auto-generated constructor stub
	}

	public void a_show(String t1) {
		if (min_labs != 0) println("minlabs(" + min_labs + ")");
		if (max_labs != Long.MAX_VALUE) println("maxlabs(" + max_labs + ")");
		println("");
		
		println("// Time slots");
		for (Timeslot timeslot : timeslots.values())//is this optimized in new java?
			println("timeslot(" + timeslot.getName() + ")");
		for (Timeslot timeslot : timeslots.values())
			for(Timeslot timeslot2 : timeslot.getConflicts())
				println("conflicts(" + timeslot.getName() + "," + timeslot2.getName() + ")");
		println("");
		
		
		println("// Instructors");
		for(Instructor instructor: instructors.values())
		{
			println("instructor(" + instructor.getName() + ")");
			for(Lecture lecture: instructor.getLectures())
			{
				//moved to course print-out
				//println("instructs(" + instructor.getName() + "," + lecture.getCourse().getName() + "," + lecture.getName() + ")");
			}
		}
		println("");
		
		println("// TAs");
		for(TA ta: tas.values())
		{
			println("TA(" + ta.getName() + ")");
		}
		println("");
		
		println("// Courses");
		for (Course course: courses.values())
		{
			if (course.isGradCourse())
			{
				println("grad-course(" + course.getName() + ")");
			} else if (course.isSeniorCourse())	{
				println("senior-course(" + course.getName() + ")");
			} else {
				println("course(" + course.getName() + ")");
			}

			for(Lecture lecture: course.getLectures())
			{
				Timeslot timeslot = lecture.getTimeslot();
				println("lecture(" + course.getName() + "," + lecture.getName() + ")");
				if (timeslot != null)
					println("at(" + course.getName() + "," + lecture.getName() + "," + timeslot.getName() + ")");
				Instructor instructor = lecture.getInstructor();
				if (instructor != null)
					println("instructs(" + instructor.getName() + "," + course.getName() + "," + lecture.getName() + ")");
			}
			
			for(Lab lab: course.getLabs())
			{
				Timeslot timeslot = lab.getTimeslot();
				if ( lab.getLecture() == null)
					{
					error("lab doesnt have lecture");
					} else {
				println("lab(" + course.getName() + "," + lab.getLecture().getName() + "," + lab.getName() + ")");
				if (timeslot != null)
					println("at(" + course.getName() + "," + lab.getName() + "," + timeslot.getName() + ")");
					}
			}
			
			println("");			
		}
		println("");
		

		println("// TA stuff");
		for(TA ta: tas.values())
		{
			for(Lab lab: ta.getLabs())
			{
			 	// TA-name, course-name, lab-name
				println("instructs(" + ta.getName() + "," + lab.getCourse().getName() + "," + lab.getName() + ")");
			}
			if (ta.getPrefer1() != null)
				println("prefers1(" + ta.getName() + "," + ta.getPrefer1().getName() + ")");
			if (ta.getPrefer2() != null)
				println("prefers2(" + ta.getName() + "," + ta.getPrefer2().getName() + ")");
			if (ta.getPrefer3() != null)
				println("prefers3(" + ta.getName() + "," + ta.getPrefer3().getName() + ")");
			for(Course course: ta.getKnows())
			{
				println("knows(" + ta.getName() + "," + course.getName() + ")");
			}
		}
		println("");
	}
	
	@Override
	public void a_maxlabs(Long p) {
		max_labs = p;
	}

	@Override
	public void a_minlabs(Long p) {
		min_labs = p;
	}

	@Override
	public void a_TA(String p) {
		if (tas.containsKey(p)) {
			println ("Warning TA already exists!");
		} else {
			tas.put(p, new TA(p));
		}
	}

	@Override
	public boolean e_TA(String p) {
		return tas.containsKey(p);
	}

	@Override
	public void a_instructor(String p) {
		if (instructors.containsKey(p)) {
			println ("Warning instructor already exists!");
		} else {
			instructors.put(p, new Instructor(p));
		}
	}

	@Override
	public boolean e_instructor(String p) {
		return instructors.containsKey(p);
	}

	@Override
	public void a_course(String p) {
		if (courses.containsKey(p)) {
			println ("Warning course already exists!");
		} else {
			courses.put(p, new Course(p));
		}
	}

	@Override
	public boolean e_course(String p) {
		return courses.containsKey(p);
	}

	@Override
	public void a_senior_course(String p) {
		Course c = courses.get(p);
		if (c == null) {
			c = new Course(p);
			courses.put(p, c);
		}
		c.setSeniorCourse(true);
	}

	@Override
	public boolean e_senior_course(String p) {
		Course c = courses.get(p);
		return c != null && c.isSeniorCourse();
	}

	@Override
	public void a_grad_course(String p) {
		Course c = courses.get(p);
		if (c == null) {
			c = new Course(p);
			courses.put(p, c);
		}
		c.setGradCourse(true);
	}

	@Override
	public boolean e_grad_course(String p) {
		Course c = courses.get(p);
		return c != null && c.isGradCourse();
	}

	@Override
	public void a_timeslot(String p) {
		if (timeslots.containsKey(p)) {
			println ("Warning time slot already exists!");		
		} else {
			timeslots.put(p, new Timeslot(p));
		}
	}

	@Override
	public boolean e_timeslot(String p) {
		return timeslots.containsKey(p);
	}

	@Override
	public void a_lecture(String c, String lec) {
		Course a = courses.get(c);
		if (a == null)
			println ("Error course does not exist, lecture will not be added!"); // Should I be throwing an exception instead? 
		else {
			if (a.hasLecture(lec))
				println("Warning lecture already exists!");
			else	
				a.addLecture(lec);
		}
		
	}

	@Override
	public boolean e_lecture(String c, String lec) {
		Course a = courses.get(c);
		return a != null && a.hasLecture(lec);
		
	}

	@Override
	public void a_lab(String c, String lec, String lab) {
		Course a = courses.get(c);
		if (a == null){
			println ("Error course does not exist, Lab will not be added!");   
			return;
		}
		
		if (!a.hasLecture(lec))
			println("Error no lecture for this course exists, lab will not be added!");
		else{
			if (!a.hasLab(lab))
				a.addLab(lab, lec);
			else
				println ("Warning lab already exists!");
		}
	}

	@Override
	public boolean e_lab(String c, String lec, String lab) {
		Course a = courses.get(c);
		return a != null && a.hasLab(lab);
	}

	@Override
	public void a_instructs(String p, String c, String l) {
		Course course = courses.get(c);
		if (c == null) {
			println ("Error course does not exist. Unable to continue!");
			return;
		}

		if (instructors.containsKey(p)) {
			Instructor instructor = instructors.get(p);
			Lecture lecture = course.getLecture(l);
			if (lecture == null) {
				println ("Error lecture does not exist. Unable to continue!");
				return;
			}
			lecture.setInstructor(instructor);
			instructor.addLecture(lecture);
		} else if (tas.containsKey(p)) {
			TA ta = tas.get(p);
			Lab lab = course.getLab(l);
			if (lab == null) {
				println(" Error lab does not exist. Unable to continue!");
				return;
			}
			lab.setTA(ta);
			ta.addLab(lab);
		}

	}

	@Override
	public boolean e_instructs(String p, String c, String l) {
		Instructor a = instructors.get(p);
		Course b = courses.get(c);
		Lecture d = b.getLecture(l);
		
		if (d.getInstructor() == a)
			return true;
		else
			return false;
	}

	@Override
	public void a_at(String c, String l, String t) {
		Timeslot timeslot = timeslots.get(t);
		if (timeslot == null) {
			error("Error time slot does not exist. Unable to continue!");
			return;
		}
		Course course = courses.get(c);
		if (course == null) {
			error("Error course does not exist. Unable to continue!");
			return;
		}
		if (course.hasLecture(l)) {
			Lecture lecture = course.getLecture(l);
			if (!lecture.hasTimeslot()){
				lecture.setTimeslot(timeslot);
				timeslot.addEntity(lecture);
				return;
			}
			else{
				error("Error lecture already has a time slot!");
				return;
			}
		}
		if (course.hasLab(l)) {
			Lab lab = course.getLab(l);
			if(!lab.hasTimeslot()){
				lab.setTimeslot(timeslot);
				timeslot.addEntity(lab);
				return;
			}
			else{
				error("Error lab already has a time slot!");
				return;
			}
		}
		else 
			error("Error Lab does not exist. Unable to continue!");
		
	}

	@Override
	public boolean e_at(String c, String l, String t) {
		Course a = courses.get(c);
		Lecture b = a.getLecture(l);
		Timeslot d = timeslots.get(t);
		
		if (b.getTimeslot() == d)
			return true;
		else
		return false;
	}

	@Override
	public void a_knows(String ta, String c) {
		TA myta = taByName(ta);
		Course mycourse = courseByName(c);
		myta.addKnows(mycourse);
	}

	@Override
	public boolean e_knows(String ta, String c) {
		TA myta = taByName(ta);
		Course course = courseByName(c);
		return myta.getKnows().contains(course);
	}

	@Override
	public void a_prefers(String instructor, String ta, String c) {
		Instructor ins = instructors.get(instructor);
		if (ins == null) {
			throw new RuntimeException("instructor " + instructor
					+ " does not exist or is not an instructor");
		} else {
			ins.addPrefers(taByName(ta), courseByName(c));
		}
	}

	@Override
	public boolean e_prefers(String instructor, String ta, String c) {
		Instructor ins = instructors.get(instructor);
		if (ins == null) {
			throw new RuntimeException("instructor " + instructor
					+ " does not exist or is not an instructor");
		} else {
			return ins.hasPrefers(taByName(ta), courseByName(c));
		}
	}

	@Override
	public void a_prefers1(String ta, String c) {
		taByName(ta).setPrefer1(courseByName(c));
	}

	@Override
	public boolean e_prefers1(String ta, String c) {
		return taByName(ta).getPrefer1().equals(courseByName(c));
	}

	@Override
	public void a_prefers2(String ta, String c) {
		taByName(ta).setPrefer2(courseByName(c));
	}

	@Override
	public boolean e_prefers2(String ta, String c) {
		return taByName(ta).getPrefer2().equals(courseByName(c));
	}

	@Override
	public void a_prefers3(String ta, String c) {
		taByName(ta).setPrefer3(courseByName(c));
	}

	@Override
	public boolean e_prefers3(String ta, String c) {
		return taByName(ta).getPrefer3().equals(courseByName(c));
	}

	@Override
	public void a_taking(String ta, String c, String l) {
		// taByName(ta).addTaking(courseByName(c),
		// courseByName(c).getLecture(l));
		TA thatGuy = taByName(ta);
		Course thatCourse = courseByName(c);
		Lecture thatLecture = thatCourse.getLecture(l);
		if(thatGuy.isTaking(thatCourse)){
			println ("Warning TA "+ thatGuy + "is already taking that course!");
			return;
		}
		else{
			if (thatCourse.getLecture(l) == null)
				throw new RuntimeException ("Lecture " + l + " does not exist.");
			else
				thatGuy.addTaking(thatCourse, thatLecture);
		}
			
	}

	@Override
	public boolean e_taking(String ta, String c, String l) {
		TA thatGuy = taByName(ta);
		Course thatCourse = courseByName(c);
		Lecture thatLecture = thatCourse.getLecture(l);
		if (thatLecture != null && thatGuy.isTaking(thatCourse) && thatGuy.withLecture(thatLecture))
			return true;
		else
			return false;
	}

	@Override
	public void a_conflicts(String t1, String t2) {
		Timeslot s1 = timeslots.get(t1);
		Timeslot s2 = timeslots.get(t2);
		if (s1 == null) {
			s1 = new Timeslot(t1);
			timeslots.put(t1, s1);
		}
		if (s2 == null) {
			s2 = new Timeslot(t2);
			timeslots.put(t2, s2);
		}
		s1.addConflict(s2);
		s2.addConflict(s1);
	}

	@Override
	public boolean e_conflicts(String t1, String t2) {
		Timeslot s1 = timeslots.get(t1);
		Timeslot s2 = timeslots.get(t2);
		return s1.conflicts(s2) || s2.conflicts(s1);
	}

	private TA taByName(String ta) {
		TA gs = tas.get(ta);
		if (gs == null) {
			error("TA " + ta + " does not exist");
		}
		return gs;
	}

	private Course courseByName(String c) {
		Course course = courses.get(c);
		if (course == null) {
			error("course " + c + " does not exist");
		}
		return course;
	}



}
