package free.jin.board;

import free.chess.ChessMove;
import free.chess.ChessPiece;
import free.chess.Move;
import free.chess.Piece;
import free.chess.Player;
import free.chess.Position;
import free.jin.Game;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class JMaterialPanel extends JPanel {

  private final Game game;
  private final Player player;
  private final JProgressBar bar;

  public JMaterialPanel(Game game, Player player) {
    super();
    bar = new JProgressBar();
    bar.setStringPainted(true);
    updateMaterial(game.getInitialPosition());
    this.game = game;
    this.player = player;
    add(bar);
  }

  public void updateMaterial(Move move) {
    if (!(move instanceof ChessMove)) {
      return;
    }

    ChessMove chessMove = (ChessMove) move;
    int newMaterial = bar.getValue();
    if (chessMove.isCapture() && chessMove.getCapturedPiece().getPlayer().equals(player)) {
      newMaterial -= game.getVariant().getApproximateMaterialValue(chessMove.getCapturedPiece());
    } else if (chessMove.isPromotion()) {
      newMaterial +=
          (game.getVariant().getApproximateMaterialValue(chessMove.getPromotionTarget())
              - game.getVariant()
                  .getApproximateMaterialValue(
                      new ChessPiece(player.getPieceColor(), ChessPiece.PAWN)));
    }

    bar.setValue(newMaterial);
    bar.setString(newMaterial + "");
  }

  public void updateMaterial(Position position) {
    int material = 0;
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        Piece piece = position.getPieceAt(i, j);
        if (piece != null && piece.getPlayer().equals(player)) {
          material += game.getVariant().getApproximateMaterialValue(piece);
        }
      }
    }
    bar.setValue(material);
    bar.setString(material + "");
  }
}
