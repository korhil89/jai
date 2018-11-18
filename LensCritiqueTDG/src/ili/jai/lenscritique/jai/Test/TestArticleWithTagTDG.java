package ili.jai.lenscritique.jai.Test;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


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
import ili.jai.lenscritique.jai.TDG.AuthorTDG;
import ili.jai.lenscritique.jai.TDG.TagTDG;
import ili.jai.tdg.api.TDGRegistry;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestArticleWithTagTDG {

	private ArticleTDG atdg;
	private AuthorTDG authtdg;
	private TagTDG ttdg;
	private List<Tag> tags = new ArrayList<>();
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
		authtdg = TDGRegistry.findTDG(Author.class);
		ttdg = TDGRegistry.findTDG(Tag.class);
		
	}
	@Test
	public void test1EmptyBase() throws SQLException {
		assertNull(atdg.findById(10));
	}
	
	@Test
	public void test2InsertingAuthor() throws SQLException {
		Author auth = new Author();
		auth.setPassword("Coucou");
		auth.setPseudo("John");
		assertEquals(0, auth.getId());
		authtdg.insert(auth);	
	}
	
	@Test
	public void test3InsertingTag() throws SQLException {
		
		Tag tag = new Tag();
		tag.setLabel("Cool");
		assertEquals(0, tag.getId());
		ttdg.insert(tag);
		Tag tag2 = new Tag();
		tag2.setLabel("Moyen Cool");
		ttdg.insert(tag2);
		tags.add(tag);
		tags.add(tag2);
		
	}
	
	@Test
	public void test4InsertingElement() throws SQLException {
		Article a = new Article();
		a.setTitle("Premier article");
		a.setContent("Ceci est un super article");
		a.setDate(LocalDate.now());
		a.setIllustration(null);

		a.setAuthor(authtdg.findById(1));
		a.setTags(tags);
		assertEquals(0, a.getId());
		atdg.insert(a);
		assertNotEquals(0, a.getId());
		Article a2 = atdg.findById(a.getId());
		assertEquals(a, a2);
		assertSame(a, a2);
	}
	@Test
	public void test5UpdatingElement() throws SQLException {
		Article a = atdg.findById(1);
		assertEquals("Premier article", a.getTitle());
		List<Tag> tags2 = new ArrayList<>();
		tags2.add(ttdg.findById(1));
		a.setTags(tags2);
		atdg.update(a);
		Article a2 = atdg.findById(1);
		assertSame(a, a2);
		assertEquals(a2.getTags(),tags2);
	}
	@Test
	public void test6DeletingElement() throws SQLException {
		Article a = atdg.findById(1);
		assertEquals("Premier article", a.getTitle());
		atdg.delete(a);
		assertNull(atdg.findById(1));
	}
	

}
