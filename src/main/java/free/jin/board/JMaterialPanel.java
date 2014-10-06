package free.jin.board;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import free.chess.ChessMove;
import free.chess.ChessPiece;
import free.chess.Move;
import free.chess.Piece;
import free.chess.Player;
import free.chess.Position;
import free.jin.Game;

public class JMaterialPanel extends JPanel {

  private final Game game;
  private final Player player;
  private final JProgressBar bar;

  public JMaterialPanel(Game game, Player player) {
    super();
    bar = new JProgressBar();
    bar.setStringPainted(true);
    int initialMaterial = 0;
    Position initialPosition = game.getInitialPosition();
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        Piece piece = initialPosition.getPieceAt(i, j);
        if (piece != null && piece.getPlayer().equals(player)) {
          initialMaterial += game.getVariant().getApproximateMaterialValue(piece);
        }
      }
    }
    bar.setValue(initialMaterial);
    bar.setString(initialMaterial + "");
    this.game = game;
    this.player = player;
    add(bar);
  }

  public void updateMaterial(Position position, Move move) {
    if (!(move instanceof ChessMove)) {
      return;
    }

    ChessMove chessMove = (ChessMove) move;
    int newMaterial = bar.getValue();
    if (chessMove.isCapture() && chessMove.getCapturedPiece().getPlayer().equals(player)) {
      newMaterial -= game.getVariant().getApproximateMaterialValue(chessMove.getCapturedPiece());
    } else if (chessMove.isPromotion()) {
      newMaterial += (game.getVariant().getApproximateMaterialValue(chessMove.getPromotionTarget()) - game
          .getVariant().getApproximateMaterialValue(
              new ChessPiece(player.getPieceColor(), ChessPiece.PAWN)));
    }

    bar.setValue(newMaterial);
    bar.setString(newMaterial + "");
  }
}
