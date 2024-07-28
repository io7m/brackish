/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */


package com.io7m.brackish.tests;

import com.io7m.brackish.core.WaveRenderStyle;
import com.io7m.brackish.core.WaveView;
import com.io7m.jranges.RangeInclusiveL;
import com.io7m.xoanon.commander.api.XCCommanderType;
import com.io7m.xoanon.commander.api.XCRobotType;
import com.io7m.xoanon.extension.XoExtension;
import javafx.scene.Scene;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicReference;

import static com.io7m.brackish.core.WaveRenderStyle.WAVE_BOXES;
import static com.io7m.brackish.core.WaveRenderStyle.WAVE_INTERPOLATE_LINEAR;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(XoExtension.class)
public final class WaveViewTest
{
  /**
   * The empty wave model has the expected properties.
   *
   * @param commander The commander
   *
   * @throws Exception On errors
   */

  @Test
  public void testModelEmpty(
    final XCCommanderType commander)
    throws Exception
  {
    final var waveView = new AtomicReference<WaveView>();
    commander.stageNewAndWait(newStage -> {
      final var view = new WaveView();
      waveView.set(view);
      newStage.setScene(new Scene(view));
    });

    final var view = waveView.get();
    final var model = view.model();
    assertEquals(0L, model.frameCount());
    assertEquals(1, model.channelCount());
  }

  /**
   * Rendering a waveform in collapsed form works.
   *
   * @param commander The commander
   * @param robot     The robot
   *
   * @throws Exception On errors
   */

  @Test
  public void testModelNoiseCollapsed(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var model =
      new WaveStereoNoise(8192);
    final var waveView =
      new AtomicReference<WaveView>();

    commander.stageNewAndWait(newStage -> {
      newStage.setMaxHeight(400);
      newStage.setMaxWidth(600);
      newStage.setMinHeight(400);
      newStage.setMinWidth(600);

      final var view = new WaveView();
      view.setWaveModel(model);
      waveView.set(view);
      newStage.setScene(new Scene(view));
    });

    final var view = waveView.get();
    robot.execute(() -> view.setViewRange(0L, 2000L));
    robot.execute(view::redraw);

    assertEquals(RangeInclusiveL.of(0L, 2000L), view.viewRange());
    assertEquals(RangeInclusiveL.of(0L, 2000L), view.viewRangeProperty().get());
    robot.waitForFrames(120);
  }

  /**
   * Rendering a waveform in collapsed form works.
   *
   * @param commander The commander
   * @param robot     The robot
   *
   * @throws Exception On errors
   */

  @Test
  public void testModelNoiseExpandedBoxes(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var model =
      new WaveStereoNoise(8192);
    final var waveView =
      new AtomicReference<WaveView>();

    commander.stageNewAndWait(newStage -> {
      newStage.setMaxHeight(400);
      newStage.setMaxWidth(600);
      newStage.setMinHeight(400);
      newStage.setMinWidth(600);

      final var view = new WaveView();
      view.setWaveModel(model);
      view.setRenderStyle(WAVE_BOXES);

      waveView.set(view);
      newStage.setScene(new Scene(view));
    });

    final var view = waveView.get();
    robot.execute(() -> view.setViewRange(0L, 128L));
    robot.execute(view::redraw);

    assertEquals(WAVE_BOXES, view.renderStyle());
    assertEquals(WAVE_BOXES, view.renderStyleProperty().get());
    assertEquals(RangeInclusiveL.of(0L, 128L), view.viewRange());
    assertEquals(RangeInclusiveL.of(0L, 128L), view.viewRangeProperty().get());
    robot.waitForFrames(120);

  }

  /**
   * Rendering a waveform in collapsed form works.
   *
   * @param commander The commander
   * @param robot     The robot
   *
   * @throws Exception On errors
   */

  @Test
  public void testModelNoiseExpandedLinear(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var model =
      new WaveStereoNoise(8192);
    final var waveView =
      new AtomicReference<WaveView>();

    commander.stageNewAndWait(newStage -> {
      newStage.setMaxHeight(400);
      newStage.setMaxWidth(600);
      newStage.setMinHeight(400);
      newStage.setMinWidth(600);

      final var view = new WaveView();
      view.setWaveModel(model);
      view.setRenderStyle(WAVE_INTERPOLATE_LINEAR);

      waveView.set(view);
      newStage.setScene(new Scene(view));
    });

    final var view = waveView.get();
    robot.execute(() -> view.setViewRange(0L, 128L));
    robot.execute(view::redraw);

    assertEquals(WAVE_INTERPOLATE_LINEAR, view.renderStyle());
    assertEquals(WAVE_INTERPOLATE_LINEAR, view.renderStyleProperty().get());
    assertEquals(RangeInclusiveL.of(0L, 128L), view.viewRange());
    assertEquals(RangeInclusiveL.of(0L, 128L), view.viewRangeProperty().get());
    robot.waitForFrames(120);
  }

  /**
   * Things don't break when provided with a microscopic window.
   *
   * @param commander The commander
   * @param robot     The robot
   *
   * @throws Exception On errors
   */

  @Test
  public void testTooSmall(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var model =
      new WaveStereoNoise(8192);
    final var waveView =
      new AtomicReference<WaveView>();

    commander.stageNewAndWait(newStage -> {
      newStage.setMaxHeight(1);
      newStage.setMaxWidth(1);
      newStage.setMinHeight(1);
      newStage.setMinWidth(1);

      final var view = new WaveView();
      view.setWaveModel(model);
      waveView.set(view);
      newStage.setScene(new Scene(view));
    });

    final var view = waveView.get();
    robot.execute(() -> view.setViewRange(0L, 2000L));
    robot.execute(view::redraw);

    assertEquals(RangeInclusiveL.of(0L, 2000L), view.viewRange());
    assertEquals(RangeInclusiveL.of(0L, 2000L), view.viewRangeProperty().get());
    robot.waitForFrames(120);
  }

  /**
   * Switching to models with smaller ranges works.
   *
   * @param commander The commander
   * @param robot     The robot
   *
   * @throws Exception On errors
   */

  @Test
  public void testDownsizeRange0(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var model0 =
      new WaveStereoNoise(8192);
    final var model1 =
      new WaveStereoNoise(128);

    final var waveView =
      new AtomicReference<WaveView>();

    commander.stageNewAndWait(newStage -> {
      newStage.setMaxHeight(1);
      newStage.setMaxWidth(1);
      newStage.setMinHeight(1);
      newStage.setMinWidth(1);

      final var view = new WaveView();
      view.setWaveModel(model0);
      waveView.set(view);
      newStage.setScene(new Scene(view));
    });

    final var view = waveView.get();
    robot.execute(() -> view.setViewRange(0L, 2000L));
    robot.execute(() -> view.setWaveModel(model1));
    robot.execute(view::redraw);

    assertEquals(RangeInclusiveL.of(0L, 127L), view.viewRange());
    assertEquals(RangeInclusiveL.of(0L, 127L), view.viewRangeProperty().get());
    robot.waitForFrames(120);
  }

  /**
   * Switching to models with smaller ranges works.
   *
   * @param commander The commander
   * @param robot     The robot
   *
   * @throws Exception On errors
   */

  @Test
  public void testDownsizeRange1(
    final XCCommanderType commander,
    final XCRobotType robot)
    throws Exception
  {
    final var model0 =
      new WaveStereoNoise(8192);
    final var model1 =
      new WaveStereoNoise(128);

    final var waveView =
      new AtomicReference<WaveView>();

    commander.stageNewAndWait(newStage -> {
      newStage.setMaxHeight(1);
      newStage.setMaxWidth(1);
      newStage.setMinHeight(1);
      newStage.setMinWidth(1);

      final var view = new WaveView();
      view.setWaveModel(model0);
      waveView.set(view);
      newStage.setScene(new Scene(view));
    });

    final var view = waveView.get();
    robot.execute(() -> view.setViewRange(1000L, 2000L));
    robot.execute(() -> view.setWaveModel(model1));
    robot.execute(view::redraw);

    assertEquals(RangeInclusiveL.of(0L, 127L), view.viewRange());
    assertEquals(RangeInclusiveL.of(0L, 127L), view.viewRangeProperty().get());
    robot.waitForFrames(120);
  }
}
