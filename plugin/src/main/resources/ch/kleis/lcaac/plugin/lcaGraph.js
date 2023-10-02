import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import * as d3Sankey from "https://cdn.jsdelivr.net/npm/d3-sankey@0.12.3/+esm";


function makeBG(elem) {
  var bounds = elem.getBBox()
  var bg = document.createElementNS("http://www.w3.org/2000/svg", "rect")
  const padding = 5
  bg.setAttribute("id", "background-display")
  bg.setAttribute("x", bounds.x - padding)
  bg.setAttribute("y", bounds.y - padding)
  bg.setAttribute("width", bounds.width + 2 * padding)
  bg.setAttribute("height", bounds.height + 2 * padding)
  bg.setAttribute("fill", "white")
  bg.setAttribute("opacity", 0.7)
  bg.setAttribute("rx", "5px")
  bg.setAttribute("stroke-width", 2) // Border
  bg.setAttribute("stroke", "#333333") // Border color
  if (elem.hasAttribute("transform")) {
    bg.setAttribute("transform", elem.getAttribute("transform"))
  }
  return bg;
}


// Specify the dimensions of the chart.
const width = 1500;
const height = (width * 9) / 16;

try {
    // Create a SVG container.
    const svg = d3
        .create("svg")
        .attr("width", width)
        .attr("height", height)
        .attr("viewBox", [0, 0, width, height])
        .attr("style", "max-width: 100%; height: auto; font: bold 16px sans-serif;");

    // Constructs and configures a Sankey generator.
    // - lambda for node ID
    // - size and spacing for nodes
    // - graph size in the window
    const sankey = d3Sankey
        .sankey()
        .nodeId((d) => d.key)
        .nodeWidth(30)
        .nodePadding(20)
        .nodeAlign(d3Sankey.sankeyLeft)
        .extent([
            [1, 20],
            [width - 1, height - 20],
        ]);

// Load our data
    const {nodes, links} = sankey({
        nodes: data.nodes,
        links: data.links,
    });

// Defines a color scale.
    const color = d3.scaleOrdinal(d3.schemePaired);

// Creates the rects that represent the nodes.
    var rect =  svg
        .append("g")
        .attr("stroke", "#000")
        .selectAll()
        .data(nodes)
        .join("rect")
        .filter((d, _) => d.value > 0.0)
        .attr("x", (d) => d.x0)
        .attr("y", (d) => d.y0)
        .attr("height", (d) => d.y1 - d.y0)
        .attr("width", (d) => d.x1 - d.x0)
        .attr("fill", (d) => color(d.name))
        .attr("id", (d)=> md5(d.key) )
        .on('mouseover', function (e, d) { // on mouse out hide line, circles and text
            const text = d3.select(".c" + e.currentTarget.id);
            text.text(d.key);
            // Change ZIndex
            const textNode = text.node()
            textNode.remove();
            svg.node().appendChild(textNode);
            const bg = makeBG(textNode);
            textNode.parentNode.insertBefore(bg, textNode)
        })
        .on('mouseout', function (e, d) { // on mouse out hide line, circles and text
            d3.select(".c" + e.currentTarget.id).text(d.name);
            d3.selectAll("#background-display")
                .each(function(d, i) {
                this.remove();
                });
        });


// Creates the paths that represent the links.
    const link = svg
        .append("g")
        .attr("fill", "none")
        .attr("stroke-opacity", 0.5)
        .selectAll()
        .data(links)
        .join("g")
        .filter((d, _) => d.value > 0.0)

    link
        .append("path")
        .attr("d", d3Sankey.sankeyLinkHorizontal())
        .attr("stroke", (d) => d3.interpolate(color(d.source.name), "grey")(0.85))
        .attr("stroke-width", (d) => Math.max(1, d.width));

// Adds labels on the nodes.
    svg
        .append("g")
        .selectAll()
        .data(nodes)
        .join("text")
        .filter((d, _) => d.value > 0.0)
        .attr("x", d => d.x0 < width / 2 ? d.x1 + 6 : d.x0 - 6)
        .attr("y", d => (d.y1 + d.y0) / 2)
        .attr("dy", "0.35em")
        .attr("text-anchor", d => d.x0 < width / 2 ? "start" : "end")
        .attr("class", d => "c" + md5(d.key))
        .text((d) => d.name);

    container.append(svg.node());

    window.link = link;
} catch {
    const errorMsg = document.createElement("p")
    errorMsg.style.color = "#707070"
    errorMsg.textContent = "Cycles in the process graph are not yet supported. Coming soon!\n"
    document.getElementById("error-container").appendChild(errorMsg)
}
