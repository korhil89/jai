package ili.jai.lenscritique.jai.Test;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import ili.jai.lenscritique.data.Tag;
import ili.jai.lenscritique.jai.TDG.TagTDG;
import ili.jai.tdg.api.TDGRegistry;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestTagTDG {

	private TagTDG ttdg;

	@BeforeClass
	public static void createTable() throws SQLException {
		TDGRegistry.findTDG(Tag.class).createTable();
	}

	@AfterClass
	public static void deleteTable() throws SQLException {
		TDGRegistry.findTDG(Tag.class).deleteTable();
	}

	@Before
	public void setUp() {
		ttdg = TDGRegistry.findTDG(Tag.class);
	}

	@Test
	public void test1EmptyBase() throws SQLException {
		assertNull(ttdg.findById(10));
	}

	@Test
	public void test2InsertingElement() throws SQLException {
		Tag t = new Tag();
		t.setLabel("Test");
		assertEquals(0, t.getId());
		ttdg.insert(t);
		assertNotEquals(0, t.getId());
		Tag t2 = ttdg.findById(t.getId());
		assertEquals(t, t2);
		assertSame(t, t2);
	}

	@Test
	public void test3UpdatingElement() throws SQLException {
		Tag t = ttdg.findById(1);
		assertEquals("Test", t.getLabel());
		t.setLabel("Bill");
		ttdg.update(t);
		Tag t2 = ttdg.findById(1);
		assertSame(t, t2);
		assertEquals("Bill", t2.getLabel());
	}

	@Test
	public void test4DeletingElement() throws SQLException {
		Tag t = ttdg.findById(1);
		assertEquals("Bill", t.getLabel());
		ttdg.delete(t);
		assertNull(ttdg.findById(1));
	}

}
