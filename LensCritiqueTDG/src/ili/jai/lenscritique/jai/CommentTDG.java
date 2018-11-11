package ili.jai.lenscritique.jai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ili.jai.lenscritique.data.Article;
import ili.jai.lenscritique.data.Comment;
import ili.jai.lenscritique.data.Tag;
import ili.jai.tdg.api.AbstractTDG;
import ili.jai.tdg.api.TDGRegistry;

public class CommentTDG extends AbstractTDG<Comment> {
	
	private Connection conn = TDGRegistry.getConnection();
	private static final String CREATE = "CREATE TABLE Comment (ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, TITLE VARCHAR(100) NOT NULL, CONTENT VARCHAR(100) NOT NULL, DATE DATE NOT NULL, AUTHOR BIGINT NOT NULL, ARTICLE BIGINT NOT NULL)";
	private static final String DROP = "DROP TABLE Comment";
	private static final String FIND_BY_ID = "SELECT ID,TITLE,CONTENT,DATE,AUTHOR,ARTICLE FROM Comment c WHERE c.ID=?";
	private static final String INSERT = "INSERT INTO Comment (TITLE,CONTENT,DATE,AUTHOR,ARTICLE) VALUES(?,?,?,?,?)";
	private static final String UPDATE = "UPDATE Comment c SET c.TITLE = ?, c.CONTENT = ?, c.DATE = ?, c.AUTHOR = ?, c.ARTICLE=? WHERE c.ID = ?";
	private static final String DELETE = "DELETE FROM Comment c WHERE c.ID = ?";
	private static final String WHERE = "SELECT ID FROM Comment c WHERE ";
	private static final String ALL = "SELECT ID FROM Comment";

	@Override
	public void createTable() throws SQLException {
		try (Statement stm = TDGRegistry.getConnection().createStatement()) {
			stm.executeUpdate(CREATE);
		}

	}

	@Override
	public void deleteTable() throws SQLException {
		try (Statement stm = TDGRegistry.getConnection().createStatement()) {
			stm.executeUpdate(DROP);
		}

	}

	@Override
	public List<Comment> selectWhere(String clauseWhereWithJoker, Object... args) throws SQLException {
		List<Comment> result = new ArrayList<>();
		try (PreparedStatement pst = TDGRegistry.getConnection().prepareStatement(WHERE + clauseWhereWithJoker)) {
			int index = 1;
			for (Object arg : args) {
				pst.setObject(index++, arg);
			}
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					result.add(findById(rs.getLong(1)));
				}
			}
		}
		return result;
	}

	@Override
	protected Comment retrieveFromDB(long id) throws SQLException {
		Comment c = null;
		try (PreparedStatement pst = TDGRegistry.getConnection().prepareStatement(FIND_BY_ID)) {
			pst.setLong(1, id);
			try (ResultSet rs = pst.executeQuery()) {
				if (rs.next()) {
					c = new Comment();
					c.setId(rs.getLong(1));
					c.setTitle(rs.getString(2));
					c.setContent(rs.getString(3));
					c.setDate(rs.getDate(4).toLocalDate());
					c.setAuthor(new AuthorTDG().retrieveFromDB(rs.getLong(5)));
					c.setArticle(new ArticleTDG().retrieveFromDB(rs.getLong(6)));
				}
			}
		}
		return c;
	}

	@Override
	protected Comment insertIntoDB(Comment c) throws SQLException {
		try (PreparedStatement pst = TDGRegistry.getConnection().prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			pst.setString(1, c.getTitle());
			pst.setString(2, c.getContent());
			pst.setDate(3, java.sql.Date.valueOf(c.getDate()));
			pst.setLong(4, c.getAuthor().getId());
			pst.setLong(5, c.getArticle().getId());
			int result = pst.executeUpdate();
			assert result == 1;
			try (ResultSet keys = pst.getGeneratedKeys()) {
				if (keys.next()) {
					c.setId(keys.getLong(1));
				}
			}
			return c;
		}
	}

	@Override
	protected Comment updateIntoDB(Comment c) throws SQLException {
		try (PreparedStatement pst = TDGRegistry.getConnection().prepareStatement(UPDATE)) {
			assert findById(c.getId()) == c;
			pst.setString(1, c.getTitle());
			pst.setString(2, c.getContent());
			pst.setDate(3, java.sql.Date.valueOf(c.getDate()));
			pst.setLong(4, c.getAuthor().getId());
			pst.setLong(5, c.getArticle().getId());
			int result = pst.executeUpdate();
			assert result == 1;
			return c;
		}
	}

	@Override
	protected Comment refreshFromDB(Comment c) throws SQLException {
		try (PreparedStatement pst = TDGRegistry.getConnection().prepareStatement(FIND_BY_ID)) {
			pst.setLong(1, c.getId());
			try (ResultSet rs = pst.executeQuery()) {
				if (rs.next()) {
					c.setId(rs.getLong(1));
					pst.setString(1, c.getTitle());
					pst.setString(2, c.getContent());
					pst.setDate(3, java.sql.Date.valueOf(c.getDate()));
					pst.setLong(4, c.getAuthor().getId());
					pst.setLong(5, c.getArticle().getId());
				}
			}
		}
		return c;
	}

	@Override
	protected Comment deleteFromDB(Comment c) throws SQLException {
		try (PreparedStatement pst = TDGRegistry.getConnection().prepareStatement(DELETE)) {
			assert findById(c.getId()) == c;
			pst.setLong(1, c.getId());
			int result = pst.executeUpdate();
			assert result == 1;
			return c;
		}
	}

	@Override
	protected List<Long> findAllIds() throws SQLException {
		List<Long> result = new ArrayList<>();
		try (PreparedStatement pst = TDGRegistry.getConnection().prepareStatement(ALL)) {
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					result.add(rs.getLong(1));
				}
			}
		}
		return result;
	}

}
