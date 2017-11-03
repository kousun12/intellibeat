package com.robc.intellibeat.listeners;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

public class TypeHandler extends TypedHandlerDelegate {
  TypeActionH listener;

  public TypeHandler(TypeActionH listener) {
    this.listener = listener;
  }

  @Override
  public Result charTyped(char c, Project project, Editor editor, PsiFile file) {
    listener.onCharType(c);
    return Result.CONTINUE;
  }
  public interface TypeActionH {
    void onCharType(char c);
  }
}

