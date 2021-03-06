/// <summary>
/// Copyright 2013 The Loon Authors
/// Licensed under the Apache License, Version 2.0 (the "License"); you may not
/// use this file except in compliance with the License. You may obtain a copy of
/// the License at
/// http://www.apache.org/licenses/LICENSE-2.0
/// Unless required by applicable law or agreed to in writing, software
/// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
/// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
/// License for the specific language governing permissions and limitations under
/// the License.
/// </summary>
///
using Loon.Core.Geom;
using Loon.Utils;
namespace Loon.Physics {
	
	public class PFigure {
	
		internal Vector2f[] done;
		internal int numVertices;
		private PVertexLoop vers;
	
		public PFigure() {
		}
	
		private float CalcArea(Vector2f v1, Vector2f v2, Vector2f v3) {
			return (v1.x - v3.x) * (v2.y - v3.y) - (v1.y - v3.y) * (v2.x - v3.x);
		}
	
		private Vector2f CheckCrossEdge(Vector2f s1, Vector2f e1, Vector2f s2,
				Vector2f e2) {
			if (s1.x == e2.x && s1.y == e2.y || e1.x == s2.x && e1.y == s2.y
					|| s1.x == s2.x && s1.y == s2.y || e1.x == e2.x && e1.y == e2.y) {
				return null;
			}
			float a1 = CalcArea(s1, e1, e2);
			float a2 = CalcArea(s1, e1, s2);
			if (a1 * a2 >= 0.0F) {
				return null;
			}
			float a3 = CalcArea(s2, e2, s1);
			float a4 = (a3 + a2) - a1;
			if (a3 * a4 >= 0.0F) {
				return null;
			} else {
				float t = a3 / (a3 - a4);
				return new Vector2f(s1.x + t * (e1.x - s1.x), s1.y + t
						* (e1.y - s1.y));
			}
		}
	
		private void InsertVertex(PVertexLoop prev, PVertexLoop add) {
			prev.next.prev = add;
			add.next = prev.next;
			add.prev = prev;
			prev.next = add;
			numVertices++;
		}
	
		private void MakeVertexList(Vector2f[] vertices) {
			vers = new PVertexLoop(vertices[0].x, vertices[0].y);
			PVertexLoop list = vers;
			int skipCount = 0;
			for (int i = 1; i < numVertices; i++) {
				if (list.v.x == vertices[i].x && list.v.y == vertices[i].y) {
					skipCount++;
				} else {
					PVertexLoop next = new PVertexLoop(vertices[i].x, vertices[i].y);
					list.next = next;
					next.prev = list;
					list = list.next;
				}
			}
			numVertices -= skipCount;
			vers.prev = list;
			list.next = vers;
			for (int i = 0; i < numVertices; i++) {
				vers.v.Set(vers.v.x + (float) MathUtils.Random() * 0.001F, vers.v.y
						+ (float) MathUtils.Random() * 0.001F);
				vers = vers.next;
			}
	
		}
	
		private void ReverseVertices(PVertexLoop begin, PVertexLoop end) {
			if (begin.next == end)
				return;
			PVertexLoop prevBegin = begin.next;
			PVertexLoop prevEnd = end.prev;
			PVertexLoop next;
			for (PVertexLoop loop = begin.next; loop != end; loop = next) {
				next = loop.next;
				loop.next = loop.prev;
				loop.prev = next;
			}
	
			prevBegin.next = end;
			end.prev = prevBegin;
			prevEnd.prev = begin;
			begin.next = prevEnd;
		}
	
		private void Figure() {
			PVertexLoop vi = vers;
			float e = 0.0001F;
			for (int i = 0; i < numVertices; i++) {
				PVertexLoop vj = vers;
				for (int j = 0; j < numVertices; j++) {
					if (vi != vj) {
						Vector2f cross = CheckCrossEdge(vi.v, vi.next.v, vj.v,
								vj.next.v);
						if (cross != null) {
							Vector2f epsilon1 = vi.v.Sub(vi.next.v);
							epsilon1.Normalize();
							epsilon1.MulLocal(e);
							Vector2f epsilon2 = vj.next.v.Sub(vj.v);
							epsilon2.Normalize();
							epsilon2.MulLocal(e);
							InsertVertex(vi, new PVertexLoop(cross.x, cross.y));
							InsertVertex(vj, new PVertexLoop(cross.x, cross.y));
							vi.next.crossPoint = true;
							vj.next.crossPoint = true;
							vi.next.pair = vj.next;
							vi.next.epsilon = epsilon1;
							vj.next.epsilon = epsilon2;
						}
					}
					vj = vj.next;
				}
	
				vi = vi.next;
			}
	
			PVertexLoop[] v = new PVertexLoop[numVertices];
			for (int i = 0; i < numVertices; i++) {
				vers = v[i] = vers.next;
			}
			for (int i = 0; i < numVertices; i++) {
				if (v[i].crossPoint && v[i].pair != null) {
					ReverseVertices(v[i], v[i].pair);
					v[i].v.AddLocal(v[i].epsilon);
					v[i].pair.v.AddLocal(v[i].pair.epsilon);
					if (CheckCrossEdge(v[i].v, v[i].next.v, v[i].pair.v,
							v[i].pair.prev.v) != null) {
						float tx = v[i].v.x;
						float ty = v[i].v.y;
						v[i].v.Set(v[i].pair.v.x, v[i].pair.v.y);
						v[i].pair.v.Set(tx, ty);
					}
				}
			}
			done = new Vector2f[numVertices];
			for (int i = 0; i < numVertices; i++) {
				done[i] = new Vector2f(vers.v.x, vers.v.y);
				vers = vers.next;
			}
	
		}

        public void Figure(Vector2f[] vertices, int numVers)
        {
			numVertices = numVers;
			MakeVertexList(vertices);
			Figure();
		}
	}
}
