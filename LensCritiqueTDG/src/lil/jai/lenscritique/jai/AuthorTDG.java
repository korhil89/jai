package lil.jai.lenscritique.jai;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ili.jai.lenscritique.data.Author;
import ili.jai.tdg.api.AbstractTDG;
import ili.jai.tdg.api.TDGRegistry;

public class AuthorTDG extends AbstractTDG<Author>{

	private Connection conn;
	private static final String CREATE = "CREATE TABLE Author (ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, PSEUDO VARCHAR(100) NOT NULL, PASSWORD VARCHAR(100) NOT NULL";
	private static final String DROP = "DROP TABLE Author";
	private static final String FIND_BY_ID = "SELECT ID,PSEUDO,PASSWORD FROM Author a WHERE a.ID=?";
	private static final String INSERT = "INSERT INTO Author (PSEUDO,PASSWORD) VALUES(?,?)";
	private static final String UPDATE = "UPDATE Author a SET a.PSEUDO = ?, a.PASSWORD = ? WHERE t.ID = ?";
	private static final String DELETE = "DELETE FROM Author a WHERE a.ID = ?";
	private static final String WHERE = "SELECT ID FROM Author a WHERE ";
	private static final String ALL = "SELECT ID FROM Author";
	
	@Override
	public void createTable() throws SQLException {
		try (Statement stm = conn.createStatement()) {
			stm.executeUpdate(CREATE);
		}
		
	}

	@Override
	public void deleteTable() throws SQLException {
		try (Statement stm = conn.createStatement()) {
			stm.executeUpdate(DROP);
		}
		
	}

	@Override
	public List<Author> selectWhere(String clauseWhereWithJoker, Object... args) throws SQLException {
		List<Author> result = new ArrayList<>();
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
	protected Author retrieveFromDB(long id) throws SQLException {
		Author a = null;
		try (PreparedStatement pst = conn.prepareStatement(FIND_BY_ID)) {
			pst.setLong(1, id);
			try (ResultSet rs = pst.executeQuery()) {
				if (rs.next()) {
					a = new Author();
					a.setId(rs.getLong(1));
					a.setPseudo(rs.getString(2));
					a.setPassword(rs.getString(3));

				}
			}
		}
		return a;
	}

	@Override
	protected Author insertIntoDB(Author a) throws SQLException {
		try (PreparedStatement pst = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			pst.setString(1, a.getPseudo());
			pst.setString(2, a.getPassword());
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
	protected Author updateIntoDB(Author a) throws SQLException {
		try (PreparedStatement pst = conn.prepareStatement(UPDATE)) {
			assert findById(a.getId()) == a;
			pst.setString(1, a.getPseudo());
			pst.setString(2, a.getPassword());
			pst.setLong(3, a.getId());
			int result = pst.executeUpdate();
			assert result == 1;
			return a;
		}
	}

	@Override
	protected Author refreshFromDB(Author a) throws SQLException {
		try (PreparedStatement pst = conn.prepareStatement(FIND_BY_ID)) {
			pst.setLong(1, a.getId());
			try (ResultSet rs = pst.executeQuery()) {
				if (rs.next()) {
					a.setId(rs.getLong(1));
					a.setPseudo(rs.getString(2));
					a.setPassword(rs.getString(3));
				}
			}
		}
		return a;
	}

	@Override
	protected Author deleteFromDB(Author a) throws SQLException {
		try (PreparedStatement pst = conn.prepareStatement(DELETE)) {
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
