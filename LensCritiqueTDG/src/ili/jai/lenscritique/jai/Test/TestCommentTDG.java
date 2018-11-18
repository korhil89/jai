package ili.jai.lenscritique.jai.Test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.time.LocalDate;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import ili.jai.lenscritique.data.Article;
import ili.jai.lenscritique.data.Author;
import ili.jai.lenscritique.data.Comment;
import ili.jai.lenscritique.data.Tag;
import ili.jai.lenscritique.jai.TDG.CommentTDG;
import ili.jai.tdg.api.TDGRegistry;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCommentTDG {

	private CommentTDG ctdg;
	@BeforeClass
	public static void createTable() throws SQLException {
		TDGRegistry.findTDG(Tag.class).createTable();
		TDGRegistry.findTDG(Author.class).createTable();
		TDGRegistry.findTDG(Article.class).createTable();
		TDGRegistry.findTDG(Comment.class).createTable();
	}
	@AfterClass
	public static void deleteTable() throws SQLException {
		TDGRegistry.findTDG(Comment.class).deleteTable();
		TDGRegistry.findTDG(Article.class).deleteTable();
		TDGRegistry.findTDG(Author.class).deleteTable();
		TDGRegistry.findTDG(Tag.class).deleteTable();
	}
	@Before
	public void setUp() {
		ctdg = TDGRegistry.findTDG(Comment.class);
	}
	@Test
	public void test1EmptyBase() throws SQLException {
		assertNull(ctdg.findById(10));
	}
	@Test
	public void test2InsertingElement() throws SQLException {
		Article a = new Article();
		a.setTitle("Premier article");
		a.setContent("Ceci est un super article");
		a.setDate(LocalDate.now());
		a.setIllustration(null);
		
		Author auth = new Author();
		auth.setPassword("Coucou");
		auth.setPseudo("John");
		assertEquals(0, auth.getId());
		a.setAuthor(auth);
		assertEquals(0, a.getId());
		
		Comment c = new Comment();
		c.setArticle(a);
		c.setAuthor(auth);
		c.setContent("Wahou");
		c.setDate(LocalDate.now());
		c.setTitle("Amazing");
		ctdg.insert(c);
		assertNotEquals(0, c.getId());
		Comment c2 = ctdg.findById(c.getId());
		assertEquals(c, c2);
		assertSame(c, c2);
	}
	@Test
	public void test3UpdatingElement() throws SQLException {
		Comment c = ctdg.findById(1);
		assertEquals("Amazing", c.getTitle());
		c.setTitle("Modif Titre");
		ctdg.update(c);
		Comment c2 = ctdg.findById(1);
		assertSame(c, c2);
		assertEquals("Modif Titre", c2.getTitle());
	}
	
	
	@Test
	public void test4RetrievingElementWithAuthor() throws SQLException {
		Comment c = ctdg.findById(1);
		assertNotNull(c);
		assertNotNull(c.getAuthor());
		assertEquals("John", c.getAuthor().getPseudo());
	}
	@Test
	public void test5RefreshElement() throws SQLException {
		Comment c = ctdg.findById(1);
		assertNotNull(c);
		assertNotNull(c.getAuthor());
		assertEquals("John", c.getAuthor().getPseudo());
		assertEquals("Modif Titre", c.getTitle());
		c.setTitle("Second bis");
		assertEquals("Second bis", c.getTitle());
		ctdg.refresh(c);
		assertEquals("Modif Titre", c.getTitle());
	}

	@Test
	public void test6DeletingElement() throws SQLException {
		Comment c = ctdg.findById(1);
		assertEquals("Modif Titre", c.getTitle());
		ctdg.delete(c);
		assertNull(ctdg.findById(1));
	}
	
}
