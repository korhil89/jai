package ili.jai.lenscritique.jai.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.sql.SQLException;


import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import ili.jai.lenscritique.data.Author;
import ili.jai.lenscritique.jai.AuthorTDG;
import ili.jai.tdg.api.TDGRegistry;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestAuthorTDG {

	private AuthorTDG atdg;

	@BeforeClass
	public static void createTable() throws SQLException {
		TDGRegistry.findTDG(Author.class).createTable();
	}

	@AfterClass
	public static void deleteTable() throws SQLException {
		TDGRegistry.findTDG(Author.class).deleteTable();
	}

	@Before
	public void setUp() {
		atdg = TDGRegistry.findTDG(Author.class);
	}

	@Test
	public void test1EmptyBase() throws SQLException {
		assertNull(atdg.findById(10));
	}

	@Test
	public void test2InsertingElement() throws SQLException {
		Author a = new Author();
		a.setPassword("Coucou");
		a.setPseudo("John");
		assertEquals(0, a.getId());
		atdg.insert(a);
		assertNotEquals(0, a.getId());
		Author a2 = atdg.findById(a.getId());
		assertEquals(a, a2);
		assertSame(a, a2);
	}

	@Test
	public void test3UpdatingElement() throws SQLException {
		Author a = atdg.findById(1);
		assertEquals("John", a.getPseudo());
		a.setPseudo("Billy");
		atdg.update(a);
		Author a2 = atdg.findById(1);
		assertSame(a, a2);
		assertEquals("Billy", a2.getPseudo());
	}

	@Test
	public void test4DeletingElement() throws SQLException {
		Author a = atdg.findById(1);
		assertEquals("Billy", a.getPseudo());
		atdg.delete(a);
		assertNull(atdg.findById(1));
	}

}
