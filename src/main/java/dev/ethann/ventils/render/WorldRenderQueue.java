package dev.ethann.ventils.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class WorldRenderQueue {
	private static final RenderPipeline LINES_ESP = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
			.withDepthStencilState(Optional.empty())
			.withLocation("ventils/lines_esp")
			.build()
	);

	private static final RenderPipeline FILLED_ESP = RenderPipelines.register(
		RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
			.withDepthStencilState(Optional.empty())
			.withLocation("ventils/filled_esp")
			.withCull(false)
			.build()
	);

	private static final net.minecraft.client.renderer.rendertype.RenderType LINES_ESP_TYPE = net.minecraft.client.renderer.rendertype.RenderType.create(
		"ventils-lines-esp",
		RenderSetup.builder(LINES_ESP)
			.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.createRenderSetup()
	);

	private static final net.minecraft.client.renderer.rendertype.RenderType FILLED_ESP_TYPE = net.minecraft.client.renderer.rendertype.RenderType.create(
		"ventils-filled-esp",
		RenderSetup.builder(FILLED_ESP)
			.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.createRenderSetup()
	);

	private static final net.minecraft.client.renderer.rendertype.RenderType FILLED_TYPE = net.minecraft.client.renderer.rendertype.RenderType.create(
		"ventils-filled",
		RenderSetup.builder(RenderPipelines.DEBUG_FILLED_BOX)
			.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.createRenderSetup()
	);

	private static final List<BoxCommand> BOXES = new ArrayList<>();
	private static final List<TextCommand> TEXTS = new ArrayList<>();
	private static final List<TracerCommand> TRACERS = new ArrayList<>();

	private WorldRenderQueue() {
	}

	public static void box(AABB aabb, Color color, boolean depth) {
		box(aabb, color, depth, true);
	}

	public static void box(AABB aabb, Color color, boolean depth, boolean fill) {
		BOXES.add(new BoxCommand(aabb, color, depth, fill));
	}

	public static void text(String value, Vec3 pos, float scale, boolean depth) {
		TEXTS.add(new TextCommand(value, pos, scale, depth));
	}

	public static void tracer(Vec3 to, Color color, boolean depth) {
		TRACERS.add(new TracerCommand(to, color, depth));
	}

	public static void flush(LevelRenderContext context) {
		PoseStack poseStack = context.poseStack();
		SubmitNodeCollector collector = context.submitNodeCollector();
		Minecraft mc = Minecraft.getInstance();
		Vec3 camera = mc.gameRenderer.mainCamera().position();

		poseStack.pushPose();
		poseStack.translate(-camera.x, -camera.y, -camera.z);
		drawBoxes(poseStack, collector);
		drawTracers(poseStack, collector, mc, camera);
		poseStack.popPose();

		drawTexts(poseStack, collector, camera, mc);

		BOXES.clear();
		TEXTS.clear();
		TRACERS.clear();
	}

	private static void drawBoxes(PoseStack poseStack, SubmitNodeCollector collector) {
		for (BoxCommand box : BOXES) {
			if (box.fill) {
				net.minecraft.client.renderer.rendertype.RenderType fillType = box.depth ? FILLED_TYPE : FILLED_ESP_TYPE;
				collector.submitCustomGeometry(poseStack, fillType, (pose, buffer) -> renderFilledBox(pose, buffer, box.aabb, box.color));
			}
			net.minecraft.client.renderer.rendertype.RenderType lineType = box.depth
				? RenderTypes.lines()
				: LINES_ESP_TYPE;
			collector.submitCustomGeometry(poseStack, lineType, (pose, buffer) -> renderOutlinedBox(pose, buffer, box.aabb, box.color));
		}
	}

	private static void renderFilledBox(PoseStack.Pose pose, VertexConsumer buffer, AABB aabb, Color color) {
		float r = color.getRed() / 255f;
		float g = color.getGreen() / 255f;
		float b = color.getBlue() / 255f;
		float a = Math.min(color.getAlpha() / 255f, 0.5f);
		float minX = (float) aabb.minX;
		float minY = (float) aabb.minY;
		float minZ = (float) aabb.minZ;
		float maxX = (float) aabb.maxX;
		float maxY = (float) aabb.maxY;
		float maxZ = (float) aabb.maxZ;

		buffer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a);

		buffer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a);
		buffer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a);
		buffer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a);
		buffer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a);

		buffer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a);
		buffer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a);

		buffer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a);
		buffer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a);
		buffer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a);

		buffer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a);
		buffer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a);

		buffer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a);
		buffer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a);
		buffer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a);
		buffer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a);
	}

	private static void renderOutlinedBox(PoseStack.Pose pose, VertexConsumer buffer, AABB aabb, Color color) {
		float r = color.getRed() / 255f;
		float g = color.getGreen() / 255f;
		float b = color.getBlue() / 255f;
		float a = color.getAlpha() / 255f;
		float minX = (float) aabb.minX;
		float minY = (float) aabb.minY;
		float minZ = (float) aabb.minZ;
		float maxX = (float) aabb.maxX;
		float maxY = (float) aabb.maxY;
		float maxZ = (float) aabb.maxZ;
		float width = 5f;

		buffer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(width);
		buffer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(width);
		buffer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(width);
		buffer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(width);
		buffer.addVertex(pose, minX, minY, minZ).setColor(r, g, b, a).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(width);
		buffer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(width);
		buffer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(width);
		buffer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(width);
		buffer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a).setNormal(-1.0f, 0.0f, 0.0f).setLineWidth(width);
		buffer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a).setNormal(-1.0f, 0.0f, 0.0f).setLineWidth(width);
		buffer.addVertex(pose, minX, maxY, minZ).setColor(r, g, b, a).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(width);
		buffer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(width);
		buffer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a).setNormal(0.0f, -1.0f, 0.0f).setLineWidth(width);
		buffer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a).setNormal(0.0f, -1.0f, 0.0f).setLineWidth(width);
		buffer.addVertex(pose, minX, minY, maxZ).setColor(r, g, b, a).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(width);
		buffer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(width);
		buffer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a).setNormal(0.0f, 0.0f, -1.0f).setLineWidth(width);
		buffer.addVertex(pose, maxX, minY, minZ).setColor(r, g, b, a).setNormal(0.0f, 0.0f, -1.0f).setLineWidth(width);
		buffer.addVertex(pose, minX, maxY, maxZ).setColor(r, g, b, a).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(width);
		buffer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a).setNormal(1.0f, 0.0f, 0.0f).setLineWidth(width);
		buffer.addVertex(pose, maxX, minY, maxZ).setColor(r, g, b, a).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(width);
		buffer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a).setNormal(0.0f, 1.0f, 0.0f).setLineWidth(width);
		buffer.addVertex(pose, maxX, maxY, minZ).setColor(r, g, b, a).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(width);
		buffer.addVertex(pose, maxX, maxY, maxZ).setColor(r, g, b, a).setNormal(0.0f, 0.0f, 1.0f).setLineWidth(width);
	}

	private static void drawTracers(PoseStack poseStack, SubmitNodeCollector collector, Minecraft mc, Vec3 cameraPos) {
		if (TRACERS.isEmpty()) {
			return;
		}
		var camera = mc.gameRenderer.mainCamera();
		Vector3f look = camera.rotation().transform(new Vector3f(0, 0, -1));
		Vec3 start = camera.position().add(look.x(), look.y(), look.z());
		for (TracerCommand tracer : TRACERS) {
			collector.submitCustomGeometry(poseStack, LINES_ESP_TYPE, (pose, buffer) -> {
				float r = tracer.color.getRed() / 255f;
				float g = tracer.color.getGreen() / 255f;
				float b = tracer.color.getBlue() / 255f;
				float a = tracer.color.getAlpha() / 255f;
				float ax = (float) start.x;
				float ay = (float) start.y;
				float az = (float) start.z;
				float bx = (float) tracer.to.x;
				float by = (float) tracer.to.y;
				float bz = (float) tracer.to.z;
				Vector3f normal = new Vector3f(bx - ax, by - ay, bz - az).normalize();
				buffer.addVertex(pose, ax, ay, az).setColor(r, g, b, a).setNormal(normal.x, normal.y, normal.z).setLineWidth(5f);
				buffer.addVertex(pose, bx, by, bz).setColor(r, g, b, a).setNormal(normal.x, normal.y, normal.z).setLineWidth(5f);
			});
		}
	}

	private static void drawTexts(PoseStack poseStack, SubmitNodeCollector collector, Vec3 camera, Minecraft mc) {
		Font font = mc.font;
		Quaternionf cameraRotation = mc.gameRenderer.mainCamera().rotation();
		for (TextCommand text : TEXTS) {
			poseStack.pushPose();
			var pose = poseStack.last().pose();
			float scaleFactor = text.scale * 0.025f;
			pose.translate((float) text.pos.x, (float) text.pos.y, (float) text.pos.z)
				.translate((float) -camera.x, (float) -camera.y, (float) -camera.z)
				.rotate(cameraRotation)
				.scale(scaleFactor, -scaleFactor, scaleFactor);
			float width = font.width(text.value);
			collector.submitText(
				poseStack,
				-width / 2f,
				0f,
				Component.literal(text.value).getVisualOrderText(),
				true,
				text.depth ? Font.DisplayMode.POLYGON_OFFSET : Font.DisplayMode.SEE_THROUGH,
				0xF000F0,
				0xFFFFFFFF,
				0,
				0
			);
			poseStack.popPose();
		}
	}

	private record BoxCommand(AABB aabb, Color color, boolean depth, boolean fill) {
	}

	private record TextCommand(String value, Vec3 pos, float scale, boolean depth) {
	}

	private record TracerCommand(Vec3 to, Color color, boolean depth) {
	}
}
