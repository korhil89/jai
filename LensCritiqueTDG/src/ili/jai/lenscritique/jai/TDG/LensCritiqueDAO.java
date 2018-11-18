package ili.jai.lenscritique.jai.TDG;

import java.sql.SQLException;
import java.util.List;

import ili.jai.lenscritique.data.Article;
import ili.jai.lenscritique.data.Author;
import ili.jai.lenscritique.data.Comment;
import ili.jai.lenscritique.data.Tag;
import ili.jai.tdg.api.TDGRegistry;

public class LensCritiqueDAO {

	public List<Article> findArticleFromAuth(Author auth) throws SQLException {
		return TDGRegistry.findTDG(Article.class).selectWhere("IDAUTHOR = ?", auth.getId());
	}
	
	public List<Comment> findCommentFromAuth(Author auth) throws SQLException {
		return TDGRegistry.findTDG(Comment.class).selectWhere("AUTHOR = ?", auth.getId());
	}
	
	public List<Comment> findArticleFromTag(Tag tag) throws SQLException {
		return TDGRegistry.findTDG(Comment.class).selectWhere("ID IN (SELECT IDARTICLE FROM Art_tag where IDTAG = ?)", tag.getId());
	}

}
