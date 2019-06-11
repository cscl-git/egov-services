import { version } from "../../package.json";
import { Router } from "express";
import update from "./update";
import create from "./create";
import search from "./search";
import calculate from "./calculate";
import getbill from "./getbill";
const asyncHandler = require("express-async-handler");

export default pool => {
  let api = Router();

  api.post(
    "/firenoc-calculator/billingslab/_create",
    asyncHandler(async (req, res, next) => create(req, res))
  );
  api.post("/firenoc-calculator/billingslab/_search", (req, res) =>
    search(req, res, pool)
  );
  api.post("/firenoc-calculator/billingslab/_update", (req, res) =>
    update(req, res)
  );
  api.post(
    "/firenoc-calculator/v1/_calculate",
    asyncHandler(async (req, res) => await calculate(req, res, pool))
  );
  api.post(
    "/firenoc-calculator/v1/_getbill",
    asyncHandler(async (req, res) => getbill(req, res))
  );

  // perhaps expose some API metadata at the root
  api.get("/", (req, res) => {
    res.json({ version });
  });
  return api;
};
