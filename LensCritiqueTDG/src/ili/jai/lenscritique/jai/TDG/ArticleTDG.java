package ili.jai.lenscritique.jai.TDG;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import ili.jai.lenscritique.data.Article;
import ili.jai.lenscritique.data.Author;
import ili.jai.lenscritique.data.Comment;
import ili.jai.lenscritique.data.Tag;
import ili.jai.tdg.api.AbstractTDG;
import ili.jai.tdg.api.TDGRegistry;

public class ArticleTDG extends AbstractTDG<Article> {

	private Connection conn = TDGRegistry.getConnection();
	private static final String CREATE = "CREATE TABLE Article (ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY, TITLE VARCHAR(100) NOT NULL, CONTENT VARCHAR(100) NOT NULL, IDAUTHOR BIGINT REFERENCES Author(ID), DATE DATE NOT NULL, ILLUSTRATION BLOB)";
	private static final String DROP = "DROP TABLE Article";
	private static final String FIND_BY_ID = "SELECT ID,TITLE,CONTENT,IDAUTHOR,DATE,ILLUSTRATION FROM Article a WHERE a.ID=?";
	private static final String INSERT = "INSERT INTO Article (TITLE,CONTENT,IDAUTHOR,DATE,ILLUSTRATION) VALUES(?,?,?,?,?)";
	private static final String UPDATE = "UPDATE Article a SET a.TITLE = ?, a.CONTENT = ?, a.IDAUTHOR = ?, a.DATE = ?,a.ILLUSTRATION = ? WHERE a.ID = ?";
	private static final String DELETE = "DELETE FROM Article a WHERE a.ID = ?";
	private static final String WHERE = "SELECT ID FROM Article a WHERE ";
	private static final String ALL = "SELECT ID FROM Article";

	private static final String CREATE2 = "CREATE TABLE Art_tag (IDARTICLE BIGINT REFERENCES Article(ID), IDTAG BIGINT REFERENCES Tag(ID))";
	private static final String DROP2 = "DROP TABLE Art_tag";
	private static final String INSERT_TAG = "INSERT INTO Art_tag (IDARTICLE, IDTAG) VALUES(?, ?)";
	private static final String FIND_TAG = "SELECT IDTAG FROM Art_tag a WHERE a.IDARTICLE = ?";
	private static final String DELETE2 = "DELETE FROM Art_tag a WHERE a.IDARTICLE = ?";

	@Override
	public void createTable() throws SQLException {
		try (Statement stm = conn.createStatement()) {
			stm.executeUpdate(CREATE);
			stm.executeUpdate(CREATE2);
		}

	}

	@Override
	public void deleteTable() throws SQLException {
		try (Statement stm = conn.createStatement()) {
			stm.executeUpdate(DROP2);
			stm.executeUpdate(DROP);
		}

	}

	@Override
	public List<Article> selectWhere(String clauseWhereWithJoker, Object... args) throws SQLException {
		List<Article> result = new ArrayList<>();
		try (PreparedStatement pst = conn.prepareStatement(WHERE + clauseWhereWithJoker)) {
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
		try (PreparedStatement pst = conn.prepareStatement(FIND_BY_ID)) {
			pst.setLong(1, id);
			try (ResultSet rs = pst.executeQuery()) {
				if (rs.next()) {
					a = new Article();
					a.setId(rs.getLong(1));
					a.setTitle(rs.getString(2));
					a.setContent(rs.getString(3));
					a.setComments(TDGRegistry.findTDG(Comment.class).selectWhere("ARTICLE = ?", a.getId()));

					long autheurId = rs.getLong(4);
					if (autheurId != 0) {
						a.setAuthor(new AuthorTDG().retrieveFromDB(autheurId));
					}
					a.setDate(rs.getDate(5).toLocalDate());
					if (rs.getBlob(6) != null) {
						try {
							BufferedImage image = ImageIO.read(rs.getBlob(6).getBinaryStream());
							a.setIllustration(image);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					a.setTags(insertTagToArticle(a.getId()));

				}
			}
		}
		return a;
	}



	@Override
	protected Article insertIntoDB(Article a) throws SQLException {
		try (PreparedStatement pst = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
			pst.setString(1, a.getTitle());
			pst.setString(2, a.getContent());
			if (a.getAuthor() == null) {
				pst.setNull(3, Types.BIGINT);
			} else {
				Author auteur = a.getAuthor();
				if (auteur.getId() == 0) {
					TDGRegistry.findTDG(Author.class).insert(auteur);
				}
				pst.setLong(3, a.getAuthor().getId());
			}
			pst.setDate(4, java.sql.Date.valueOf(a.getDate()));

			if (a.getIllustration() == null) {
				pst.setNull(5, Types.BLOB);
			} else {
				pst.setBlob(5, imageToBlob(a.getIllustration()));
			}

			addTagToDB(a.getId(),a.getTags());

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
		try (PreparedStatement pst = conn.prepareStatement(UPDATE)) {
			assert findById(a.getId()) == a;
			pst.setString(1, a.getTitle());
			pst.setString(2, a.getContent());
			if (a.getAuthor() == null) {
				pst.setNull(3, Types.BIGINT);
			} else {
				Author auteur = a.getAuthor();
				if (auteur.getId() == 0) {
					TDGRegistry.findTDG(Author.class).insert(auteur);
				} else {
					TDGRegistry.findTDG(Author.class).update(auteur);
				}
				pst.setLong(3, a.getAuthor().getId());
			}
			pst.setDate(4, java.sql.Date.valueOf(a.getDate()));
			if (a.getIllustration() == null) {
				pst.setNull(5, Types.BLOB);
			} else {
				pst.setBlob(5, imageToBlob(a.getIllustration()));
			}
			pst.setLong(6, a.getId());
			deleteTagFromDB(a.getId());
			addTagToDB(a.getId(),a.getTags());

			int result = pst.executeUpdate();
			assert result == 1;
			return a;
		}
	}

	@Override
	protected Article refreshFromDB(Article a) throws SQLException {
		try (PreparedStatement pst = conn.prepareStatement(FIND_BY_ID)) {
			pst.setLong(1, a.getId());
			try (ResultSet rs = pst.executeQuery()) {
				if (rs.next()) {
					a.setId(rs.getLong(1));
					a.setTitle(rs.getString(2));
					a.setContent(rs.getString(3));
					long auteurId = rs.getLong(4);
					if (auteurId != 0) {
						a.setAuthor(TDGRegistry.findTDG(Author.class).findById(auteurId));
					}
					a.setDate(rs.getDate(5).toLocalDate());
					a.setComments(TDGRegistry.findTDG(Comment.class).selectWhere("ARTICLE = ?", a.getId()));
					if (rs.getBlob(6) != null) {
						try {
							BufferedImage image = ImageIO.read(rs.getBlob(6).getBinaryStream());
							a.setIllustration(image);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					a.setTags(insertTagToArticle(a.getId()));
				}
			}
		}
		return a;
	}

	@Override
	protected Article deleteFromDB(Article a) throws SQLException {
		try (PreparedStatement pst = conn.prepareStatement(DELETE)) {
			assert findById(a.getId()) == a;
			for (Comment comment : a.getComments()) {
				TDGRegistry.findTDG(Comment.class).delete(comment);
			}
			deleteTagFromDB(a.getId());
			pst.setLong(1, a.getId());
			int result = pst.executeUpdate();
			assert result == 1;
			return a;
		}
	}

	@Override
	protected List<Long> findAllIds() throws SQLException {
		List<Long> result = new ArrayList<>();
		try (PreparedStatement pst = conn.prepareStatement(ALL)) {
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					result.add(rs.getLong(1));
				}
			}
		}
		return result;
	}

	public Blob imageToBlob(Image img) throws SQLException {
		BufferedImage buffered = new BufferedImage(img.getWidth(null), img.getHeight(null),
				BufferedImage.TYPE_INT_ARGB);
		buffered.getGraphics().drawImage(img, 0, 0, null);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(buffered, "jpg", baos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] imageInByte = baos.toByteArray();
		Blob blob = conn.createBlob();
		blob.setBytes(1, imageInByte);
		return blob;
	}
	
	private List<Tag> insertTagToArticle(Long id) throws SQLException {
		try (PreparedStatement pst = conn.prepareStatement(FIND_TAG)) {
			pst.setLong(1, id);
			List<Tag> tags = new ArrayList<>();
			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next())
					tags.add(TDGRegistry.findTDG(Tag.class).findById(rs.getLong(1)));
			}
			return tags;
		}
	}
	
	private void addTagToDB(Long idArticle, List<Tag> listTag) throws SQLException {
		for (Tag tag : listTag) {
			if (tag.getId() == 0) {
				TDGRegistry.findTDG(Tag.class).insert(tag);
			}
			try (final PreparedStatement pst = TDGRegistry.getConnection().prepareStatement(INSERT_TAG)) {
				pst.setLong(1, idArticle);
				pst.setLong(2, tag.getId());
				int resultTag = pst.executeUpdate();
				assert resultTag == 1;
			}
		}
	}
	
	private void deleteTagFromDB(Long id) throws SQLException {
		try (PreparedStatement pst = conn.prepareStatement(DELETE2)) {
			pst.setLong(1, id);
			int resultTag = pst.executeUpdate();
			assert resultTag >= 0;
		}
	}

}
