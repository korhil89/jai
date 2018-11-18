package ili.jai.lenscritique.jai.Test;

import static org.junit.Assert.*;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.imageio.ImageIO;

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
import ili.jai.lenscritique.jai.TDG.ArticleTDG;
import ili.jai.tdg.api.TDGRegistry;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestArticleTDG {

	private ArticleTDG atdg;

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
		atdg = TDGRegistry.findTDG(Article.class);
	}

	@Test
	public void test1EmptyBase() throws SQLException {
		assertNull(atdg.findById(10));
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
		atdg.insert(a);
		assertNotEquals(0, a.getId());
		Article a2 = atdg.findById(a.getId());
		assertEquals(a, a2);
		assertSame(a, a2);
	}

	@Test
	public void test3UpdatingElement() throws SQLException {
		Article a = atdg.findById(1);
		assertEquals("Premier article", a.getTitle());
		a.setTitle("Modif Titre");
		atdg.update(a);
		Article a2 = atdg.findById(1);
		assertSame(a, a2);
		assertEquals("Modif Titre", a2.getTitle());
	}

	@Test
	public void test4DeletingElement() throws SQLException {
		Article a = atdg.findById(1);
		assertEquals("Modif Titre", a.getTitle());
		atdg.delete(a);
		assertNull(atdg.findById(1));
	}

	@Test
	public void test5InsertingElementWithTag() throws SQLException {
		Article a = new Article();
		Author auth = new Author();
		auth.setPassword("Coucou");
		auth.setPseudo("John");
		assertEquals(0, auth.getId());
		a.setAuthor(auth);
		a.setTitle("Second article");
		a.setContent("Ceci est un article pas ouf");
		a.setDate(LocalDate.of(2009, 6, 23));
		a.setIllustration(null);
		atdg.insert(a);
		assertNotEquals(0, a.getId());

	}

	@Test
	public void test6RetrievingElementWithAuthor() throws SQLException {
		Article a = atdg.findById(2);
		assertNotNull(a);
		assertNotNull(a.getAuthor());
		assertEquals("John", a.getAuthor().getPseudo());
	}

	@Test
	public void test7RefreshElement() throws SQLException {
		Article a = atdg.findById(2);
		assertNotNull(a);
		assertNotNull(a.getAuthor());
		assertEquals("John", a.getAuthor().getPseudo());
		assertEquals("Second article", a.getTitle());
		a.setTitle("Second bis");
		assertEquals("Second bis", a.getTitle());
		atdg.refresh(a);
		assertEquals("Second article", a.getTitle());
	}

	@Test
	public void test8InsertingElementWithImage() throws SQLException {
		String urlImg = "https://vignette.wikia.nocookie.net/halo/images/1/12/Gears-of-War-Skull-2-256x256.png";
		Article a = new Article();
		a.setTitle("Article avec image");
		a.setContent("Testons ...");
		a.setDate(LocalDate.now());

		try {
			URL url = new URL(urlImg);
			Image img = ImageIO.read(url);
			a.setIllustration(img);

			Author auth = new Author();
			auth.setPassword("ppwd");
			auth.setPseudo("Billy");
			a.setAuthor(auth);
			atdg.insert(a);
		} catch (IOException e) {
			e.printStackTrace();
			a.setIllustration(null);
		}

	}

}
