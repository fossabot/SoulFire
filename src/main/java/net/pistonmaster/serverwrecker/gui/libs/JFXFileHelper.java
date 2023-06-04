/*
 * ServerWrecker
 *
 * Copyright (C) 2023 ServerWrecker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 */
package net.pistonmaster.serverwrecker.gui.libs;

import javafx.application.Platform;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class JFXFileHelper {
    public static Path showOpenDialog(FileChooser fileChooser) {
        CompletableFuture<Path> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                future.complete(file.toPath());
            } else {
                future.complete(null);
            }
        });
        return future.join();
    }

    public static Path showSaveDialog(FileChooser fileChooser) {
        CompletableFuture<Path> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                future.complete(file.toPath());
            } else {
                future.complete(null);
            }
        });
        return future.join();
    }
}
