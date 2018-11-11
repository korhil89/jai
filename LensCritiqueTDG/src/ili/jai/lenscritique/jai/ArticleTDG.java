package ili.jai.lenscritique.jai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.derby.iapi.types.SQLDate;

import ili.jai.lenscritique.data.Article;
import ili.jai.lenscritique.data.Author;
import ili.jai.tdg.api.AbstractTDG;
import ili.jai.tdg.api.TDGRegistry;

public class ArticleTDG extends AbstractTDG<Article>{

	private Connection conn;
	private static final String CREATE = "CREATE TABLE Article (ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, TITLE VARCHAR(100) NOT NULL, CONTENT VARCHAR(100) NOT NULL, IDAUTHOR BIGINT NOT NULL, DATE DATE NOT NULL, ILLUSTRATION)";
	private static final String DROP = "DROP TABLE Article";
	private static final String FIND_BY_ID = "SELECT ID,TITLE,CONTENT,IDAUTHOR,DATE,ILLUSTRATION FROM Article a WHERE a.ID=?";
	private static final String INSERT = "INSERT INTO Article (TITLE,CONTENT,IDAUTHOR,DATE,ILLUSTRATION) VALUES(?,?,?,?,?)";
	private static final String UPDATE = "UPDATE Article a SET a.TITLE = ?, a.CONTENT = ?, a.IDAUTHOR = ?, a.DATE = ?,a.ILLUSTRATION = ? WHERE t.ID = ?";
	private static final String DELETE = "DELETE FROM Article a WHERE a.ID = ?";
	private static final String WHERE = "SELECT ID FROM Article a WHERE ";
	private static final String ALL = "SELECT ID FROM Article";
	
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
	public List<Article> selectWhere(String clauseWhereWithJoker, Object... args) throws SQLException {
		List<Article> result = new ArrayList<>();
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
	protected Article retrieveFromDB(long id) throws SQLException {
		Article a = null;
		try (PreparedStatement pst = TDGRegistry.getConnection().prepareStatement(FIND_BY_ID)) {
			pst.setLong(1, id);
			try (ResultSet rs = pst.executeQuery()) {
				if (rs.next()) {
					a = new Article();
					a.setId(rs.getLong(1));
					a.setTitle(rs.getString(2));
					a.setContent(rs.getString(3));
					a.setAuthor(new AuthorTDG().retrieveFromDB(rs.getLong(4)));
					a.setDate(rs.getDate(5).toLocalDate());
					a.setIllustration(rs.getBlob(6));
				}
			}
		}
		return a;
	}

	@Override
	protected Article insertIntoDB(Article a) throws SQLException {
		try (PreparedStatement pst = TDGRegistry.getConnection().prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			pst.setString(1, a.getTitle());
			pst.setString(2, a.getContent());
			pst.setLong(3, a.getAuthor().getId());
			pst.setDate(4, java.sql.Date.valueOf(a.getDate()));
			pst.setBlob(5, );
			int result = pst.executeUpdate();
			assert result == 1;
			try (ResultSet keys = pst.getGeneratedKeys()) {
				if (keys.next()) {
					a.setId(keys.getLong(1));
				}
			}
			return a;
		}
	}

	@Override
	protected Article updateIntoDB(Article a) throws SQLException {
		try (PreparedStatement pst = TDGRegistry.getConnection().prepareStatement(UPDATE)) {
			assert findById(a.getId()) == a;
			pst.setString(1, a.getTitle());
			pst.setString(2, a.getContent());
			pst.setLong(3,  a.getAuthor().getId());
			pst.setDate(4, java.sql.Date.valueOf(a.getDate()));
			pst.setBlob(5, );
			int result = pst.executeUpdate();
			assert result == 1;
			return a;
		}
	}

	@Override
	protected Article refreshFromDB(Article a) throws SQLException {
		try (PreparedStatement pst = TDGRegistry.getConnection().prepareStatement(FIND_BY_ID)) {
			pst.setLong(1, a.getId());
			try (ResultSet rs = pst.executeQuery()) {
				if (rs.next()) {
					a.setId(rs.getLong(1));
					pst.setString(1, a.getTitle());
					pst.setString(2, a.getContent());
					pst.setLong(3,  a.getAuthor().getId());
					pst.setDate(4, java.sql.Date.valueOf(a.getDate()));
					pst.setBlob(5, );
				}
			}
		}
		return a;
	}

	@Override
	protected Article deleteFromDB(Article a) throws SQLException {
		try (PreparedStatement pst = TDGRegistry.getConnection().prepareStatement(DELETE)) {
			assert findById(a.getId()) == a;
			pst.setLong(1, a.getId());
			int result = pst.executeUpdate();
			assert result == 1;
			return a;
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
